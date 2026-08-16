package br.com.sebratel.bff.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    @Value("${spring.security.user.name}")
    private String username;

    @Value("${spring.security.user.password}")
    private String password;

    @Value("${security.audit.enabled:true}")
    private boolean auditEnabled;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(request -> {
                    var corsConfiguration = new CorsConfiguration();
                    corsConfiguration.setAllowedOrigins(List.of("*"));
                    corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                    corsConfiguration.setAllowedHeaders(List.of("*"));
                    return corsConfiguration;
                }))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // O dispatch de erro do Spring passa pelo filtro de seguranca; sem isso um 500
                        // real vira 401 e a causa original se perde.
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/afetados/contract/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/matrix").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").authenticated()
                        .requestMatchers("/api/v1/token/google").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(jwtDecoder()))
                        .authenticationEntryPoint((request, response, authException) -> {
                            log.warn("Acesso negado: {}", authException.getMessage());
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write("Acesso restrito: Token inválido ou não fornecido.");
                        })
                )
                // Auditoria somente-leitura, posicionada apos o AuthorizationFilter para
                // que o principal ja esteja resolvido. Ver AuthAuditFilter.
                .addFilterAfter(new AuthAuditFilter(auditEnabled), AuthorizationFilter.class);

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri("https://www.googleapis.com/oauth2/v3/certs").build();
        jwtDecoder.setJwtValidator(jwtValidator());
        return jwtDecoder;
    }

    /**
     * Validadores aplicados apos a verificacao de assinatura contra o JWKS do Google.
     *
     * <p>Extraido do {@link #jwtDecoder()} para poder ser testado sem depender de rede:
     * o decoder faz fetch do JWKS, os validadores nao.
     *
     * <p>ATENCAO: a claim {@code aud} nao e validada aqui. Isso significa que um ID token
     * emitido pelo Google para qualquer outro OAuth client e aceito, desde que o email
     * pertenca ao dominio. Correcao pendente (finding F-03) -- exige levantar previamente
     * todos os client IDs legitimos (web e mobile), sob pena de derrubar um dos front-ends.
     */
    static OAuth2TokenValidator<Jwt> jwtValidator() {
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer("https://accounts.google.com");
        OAuth2TokenValidator<Jwt> sebratelValidator = new JwtClaimValidator<String>("email",
                email -> email != null && email.endsWith("@sebratel.com.br"));
        return new DelegatingOAuth2TokenValidator<>(withIssuer, sebratelValidator);
    }

    @Bean
    public org.springframework.security.core.userdetails.UserDetailsService userDetailsService() {
        var user = org.springframework.security.core.userdetails.User.builder()
                .username(username)
                .password("{noop}" + password)
                .roles("USER")
                .build();

        return new org.springframework.security.provisioning.InMemoryUserDetailsManager(user);
    }
}