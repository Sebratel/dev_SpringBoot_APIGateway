package br.com.sebratel.bff.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Cobre o {@link AuthAuditFilter}.
 *
 * <p>Existe porque o filtro foi acrescentado a cadeia de seguranca de PRODUCAO. O
 * finding F-17 desta auditoria era exatamente "codigo de seguranca sem teste";
 * adicionar um componente novo a essa cadeia sem cobri-lo repetiria o problema
 * que a auditoria apontou.
 *
 * <p>Tres garantias importam aqui, e os testes abaixo as provam: o filtro nunca
 * interrompe a requisicao, nao altera a resposta, e nao permite que um header
 * controlado pelo cliente forje linhas no arquivo de log.
 */
class AuthAuditFilterTest {

    private ListAppender<ILoggingEvent> appender;
    private Logger auditLogger;

    @BeforeEach
    void capturarLogs() {
        auditLogger = (Logger) LoggerFactory.getLogger("AUTH_AUDIT");
        appender = new ListAppender<>();
        appender.start();
        auditLogger.addAppender(appender);
    }

    @AfterEach
    void limpar() {
        auditLogger.detachAppender(appender);
        SecurityContextHolder.clearContext();
    }

    private String linhaRegistrada() {
        assertThat(appender.list).hasSize(1);
        return appender.list.get(0).getFormattedMessage();
    }

    private void anonimoNoContexto() {
        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken("chave", "anonymousUser",
                        AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
    }

    private MockFilterChain executar(AuthAuditFilter filtro, MockHttpServletRequest request) throws Exception {
        MockFilterChain chain = new MockFilterChain();
        filtro.doFilter(request, new MockHttpServletResponse(), chain);
        return chain;
    }

    @Test
    @DisplayName("Desligado nao registra nada, mas a requisicao segue normalmente")
    void desligadoNaoRegistraMasNaoInterrompe() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/afetados");

        MockFilterChain chain = executar(new AuthAuditFilter(false), request);

        assertThat(appender.list).isEmpty();
        assertThat(chain.getRequest())
                .as("a cadeia precisa continuar mesmo com a auditoria desligada")
                .isNotNull();
    }

    @Test
    @DisplayName("Requisicao anonima registra as pistas de origem")
    void anonimaRegistraOrigem() throws Exception {
        anonimoNoContexto();

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/matrix");
        request.addHeader("User-Agent", "curl/8.0");
        request.addHeader("Origin", "https://exemplo.com.br");

        executar(new AuthAuditFilter(true), request);

        assertThat(linhaRegistrada())
                .contains("mechanism=anonymous")
                .contains("path=/api/v1/matrix")
                .contains("curl/8.0")
                .contains("https://exemplo.com.br");
    }

    @Test
    @DisplayName("JWT registra o email do funcionario, nunca o valor do token")
    void jwtRegistraEmail() throws Exception {
        Jwt jwt = Jwt.withTokenValue("valor-secreto-do-token")
                .header("alg", "RS256")
                .claim("email", "alguem@sebratel.com.br")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
        // Construtor COM authorities: o de um argumento so deixa isAuthenticated()
        // em false, e o filtro (corretamente) classificaria como anonimo. E com
        // authorities que o resource server real popula o contexto.
        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(jwt, AuthorityUtils.createAuthorityList("SCOPE_openid")));

        executar(new AuthAuditFilter(true), new MockHttpServletRequest("GET", "/api/v1/afetados"));

        assertThat(linhaRegistrada())
                .contains("mechanism=jwt")
                .contains("subject=alguem@sebratel.com.br")
                .doesNotContain("valor-secreto-do-token");
    }

    @Test
    @DisplayName("HTTP Basic e identificado como basic, que e o dado que o finding F-02 precisa")
    void basicEIdentificado() throws Exception {
        var principal = User.builder().username("integrador").password("x").roles("USER").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, "x", principal.getAuthorities()));

        executar(new AuthAuditFilter(true), new MockHttpServletRequest("POST", "/api/v1/massivas"));

        assertThat(linhaRegistrada())
                .contains("mechanism=basic")
                .contains("subject=integrador");
    }

    @Test
    @DisplayName("Quebra de linha em header nao forja uma linha falsa no log")
    void headerComQuebraDeLinhaNaoForjaLog() throws Exception {
        anonimoNoContexto();

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/matrix");
        request.addHeader("User-Agent", "malicioso\r\nmechanism=jwt subject=admin@sebratel.com.br");

        executar(new AuthAuditFilter(true), request);

        String linha = linhaRegistrada();
        assertThat(linha)
                .as("CR e LF precisam ser neutralizados antes de chegar ao log")
                .doesNotContain("\r")
                .doesNotContain("\n");
        assertThat(linha).contains("mechanism=anonymous");
    }

    @Test
    @DisplayName("Header gigante e truncado, para nao inundar o arquivo de log")
    void headerGiganteETruncado() throws Exception {
        anonimoNoContexto();

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/matrix");
        request.addHeader("User-Agent", "A".repeat(5000));

        executar(new AuthAuditFilter(true), request);

        assertThat(linhaRegistrada())
                .contains("...")
                .hasSizeLessThan(1000);
    }

    @Test
    @DisplayName("Falha interna da auditoria nao pode derrubar a requisicao")
    void falhaInternaNaoDerrubaARequisicao() throws Exception {
        anonimoNoContexto();

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/v1/matrix");
        when(request.getRemoteAddr()).thenThrow(new IllegalStateException("falha simulada"));

        MockFilterChain chain = new MockFilterChain();
        new AuthAuditFilter(true).doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(chain.getRequest())
                .as("a requisicao real precisa seguir mesmo se a auditoria quebrar")
                .isNotNull();
        assertThat(appender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .anySatisfy(mensagem -> assertThat(mensagem).contains("Falha ao auditar"));
    }

    @Test
    @DisplayName("Sem autenticacao no contexto, trata como anonimo em vez de estourar")
    void semAutenticacaoTrataComoAnonimo() throws Exception {
        SecurityContextHolder.clearContext();

        executar(new AuthAuditFilter(true), new MockHttpServletRequest("GET", "/api/v1/matrix"));

        assertThat(linhaRegistrada()).contains("mechanism=anonymous");
    }

    @Test
    @DisplayName("Nao mexe na resposta")
    void naoAlteraAResposta() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        new AuthAuditFilter(true).doFilter(
                new MockHttpServletRequest("GET", "/api/v1/matrix"), response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeaderNames()).isEmpty();
    }

    @Test
    @DisplayName("Cada requisicao gera exatamente uma linha")
    void umaLinhaPorRequisicao() throws Exception {
        AuthAuditFilter filtro = new AuthAuditFilter(true);

        for (String path : List.of("/a", "/b", "/c")) {
            executar(filtro, new MockHttpServletRequest("GET", path));
        }

        assertThat(appender.list).hasSize(3);
    }
}
