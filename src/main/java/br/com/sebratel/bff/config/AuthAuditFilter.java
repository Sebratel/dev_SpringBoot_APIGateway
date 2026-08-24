package br.com.sebratel.bff.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de observacao que registra, por requisicao, qual mecanismo de autenticacao
 * foi efetivamente usado.
 *
 * <p>Existe para responder duas perguntas antes de endurecer as regras de seguranca,
 * sem arriscar quebrar consumidores desconhecidos:
 *
 * <ul>
 *   <li>quais rotas ainda sao acessadas via HTTP Basic (credencial estatica compartilhada);</li>
 *   <li>quem consome as rotas publicas ({@code /api/v1/matrix} e
 *       {@code /api/v1/afetados/contract/**}) sem autenticacao.</li>
 * </ul>
 *
 * <p>O filtro apenas le e registra: nao altera request, response, status nem o
 * SecurityContext. Qualquer falha interna e engolida para que a auditoria jamais
 * derrube uma requisicao real.
 *
 * <p>Registrado manualmente em {@link SecurityConfig} depois do
 * {@code AuthorizationFilter}, pois so nesse ponto o principal ja esta resolvido.
 * Por nao ser um {@code @Component}, nao e registrado em duplicidade pelo
 * auto-registro de filtros do Spring Boot.
 */
public class AuthAuditFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger("AUTH_AUDIT");

    /** Limite defensivo para valores vindos do cliente, evitando poluir o log. */
    private static final int MAX_HEADER_LENGTH = 120;

    private final boolean enabled;

    public AuthAuditFilter(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !enabled;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            audit(request);
        } catch (Exception e) {
            // Auditoria nunca pode interromper o fluxo da aplicacao.
            log.warn("Falha ao auditar autenticacao da requisicao: {}", e.getMessage());
        }
        filterChain.doFilter(request, response);
    }

    private void audit(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String mechanism = resolveMechanism(authentication);
        String subject = resolveSubject(authentication);

        if ("anonymous".equals(mechanism)) {
            // Rotas publicas: interessa descobrir QUEM chama, entao registramos as pistas
            // de origem. Todos esses valores sao controlados pelo cliente e servem apenas
            // para diagnostico -- nenhuma decisao de seguranca e tomada a partir deles.
            log.info("mechanism=anonymous method={} path={} remoteAddr={} userAgent=\"{}\" origin=\"{}\" referer=\"{}\" xff=\"{}\"",
                    request.getMethod(),
                    sanitize(request.getRequestURI()),
                    request.getRemoteAddr(),
                    sanitize(request.getHeader("User-Agent")),
                    sanitize(request.getHeader("Origin")),
                    sanitize(request.getHeader("Referer")),
                    sanitize(request.getHeader("X-Forwarded-For")));
            return;
        }

        log.info("mechanism={} method={} path={} subject={}",
                mechanism,
                request.getMethod(),
                sanitize(request.getRequestURI()),
                subject);
    }

    private String resolveMechanism(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return "anonymous";
        }
        if (authentication instanceof JwtAuthenticationToken) {
            return "jwt";
        }
        // O UsernamePasswordAuthenticationToken produzido aqui vem exclusivamente do
        // httpBasic configurado em SecurityConfig.
        if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            return "basic";
        }
        return authentication.getClass().getSimpleName();
    }

    private String resolveSubject(Authentication authentication) {
        if (authentication == null) {
            return "-";
        }
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            String email = jwt.getClaimAsString("email");
            return email != null ? email : "-";
        }
        return sanitize(authentication.getName());
    }

    /**
     * Remove quebras de linha e trunca. Valores vindos de header podem conter CR/LF,
     * o que permitiria forjar linhas falsas no arquivo de log.
     */
    private String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        String cleaned = value.replaceAll("[\\r\\n\\t]", "_");
        return cleaned.length() > MAX_HEADER_LENGTH
                ? cleaned.substring(0, MAX_HEADER_LENGTH) + "..."
                : cleaned;
    }
}
