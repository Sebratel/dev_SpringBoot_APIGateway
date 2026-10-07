package br.com.sebratel.bff.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Cobre os validadores aplicados ao JWT depois da verificacao de assinatura.
 *
 * <p>Testa {@link SecurityConfig#jwtValidator()} isoladamente, sem rede: o
 * {@code NimbusJwtDecoder} busca o JWKS do Google, mas os validadores nao dependem
 * disso. Foi para viabilizar este teste que os validadores foram extraidos do bean
 * do decoder.
 *
 * <p>Estes testes tambem documentam uma lacuna conhecida: a claim {@code aud} NAO e
 * validada hoje (finding F-03). O ultimo teste registra esse comportamento de
 * proposito -- quando o F-03 for corrigido, ele deve falhar e ser atualizado junto.
 * E o sinal de alerta que hoje nao existe.
 */
class JwtValidatorTest {

    private static final String ISSUER_GOOGLE = "https://accounts.google.com";

    private final OAuth2TokenValidator<Jwt> validador = SecurityConfig.jwtValidator();

    private Jwt.Builder tokenBase() {
        return Jwt.withTokenValue("token-de-teste")
                .header("alg", "RS256")
                .claim("iss", ISSUER_GOOGLE)
                .claim("email", "funcionario@sebratel.com.br")
                .issuedAt(Instant.now().minus(1, ChronoUnit.MINUTES))
                .expiresAt(Instant.now().plus(5, ChronoUnit.MINUTES));
    }

    private OAuth2TokenValidatorResult validar(Jwt jwt) {
        return validador.validate(jwt);
    }

    @Test
    @DisplayName("Token do Google com email do dominio Sebratel e aceito")
    void tokenValidoEAceito() {
        assertThat(validar(tokenBase().build()).hasErrors()).isFalse();
    }

    @Test
    @DisplayName("Token expirado e rejeitado")
    void tokenExpiradoERejeitado() {
        Jwt expirado = tokenBase()
                .issuedAt(Instant.now().minus(2, ChronoUnit.HOURS))
                .expiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .build();

        assertThat(validar(expirado).hasErrors()).isTrue();
    }

    @Test
    @DisplayName("Token de outro emissor e rejeitado")
    void emissorDiferenteERejeitado() {
        Jwt outroIssuer = tokenBase().claim("iss", "https://accounts.exemplo.com").build();

        assertThat(validar(outroIssuer).hasErrors()).isTrue();
    }

    @Test
    @DisplayName("Email fora do dominio sebratel.com.br e rejeitado")
    void emailForaDoDominioERejeitado() {
        Jwt externo = tokenBase().claim("email", "pessoa@gmail.com").build();

        assertThat(validar(externo).hasErrors()).isTrue();
    }

    @Test
    @DisplayName("Dominio que apenas parece o da Sebratel e rejeitado")
    void dominioParecidoERejeitado() {
        // A checagem usa endsWith("@sebratel.com.br") -- com a arroba. E ela que
        // impede que um dominio registrado como "naosebratel.com.br" passe.
        // Este teste existe para travar essa arroba no lugar: se alguem
        // "simplificar" para endsWith("sebratel.com.br"), o teste quebra.
        Jwt dominioColado = tokenBase().claim("email", "invasor@naosebratel.com.br").build();
        Jwt subdominio = tokenBase().claim("email", "invasor@sebratel.com.br.exemplo.com").build();

        assertThat(validar(dominioColado).hasErrors())
                .as("naosebratel.com.br nao pode ser aceito")
                .isTrue();
        assertThat(validar(subdominio).hasErrors())
                .as("sebratel.com.br usado como prefixo de outro dominio nao pode ser aceito")
                .isTrue();
    }

    @Test
    @DisplayName("Token sem a claim email e rejeitado")
    void semEmailERejeitado() {
        Jwt semEmail = Jwt.withTokenValue("token-de-teste")
                .header("alg", "RS256")
                .claim("iss", ISSUER_GOOGLE)
                .issuedAt(Instant.now().minus(1, ChronoUnit.MINUTES))
                .expiresAt(Instant.now().plus(5, ChronoUnit.MINUTES))
                .build();

        assertThat(validar(semEmail).hasErrors()).isTrue();
    }

    @Test
    @DisplayName("COMPORTAMENTO ATUAL (F-03): audience nao e validada")
    void audienceNaoEValidada() {
        // Um ID token emitido pelo Google para QUALQUER outro OAuth client e aceito,
        // desde que o email pertenca ao dominio. Cenario concreto: um funcionario faz
        // login com Google em um site de terceiros e o operador daquele site pode
        // replayar o token aqui.
        //
        // Quando o F-03 for corrigido, este teste deve passar a falhar. Atualize-o
        // junto com a correcao -- ele existe para avisar, nao para congelar a falha.
        Jwt deOutroApp = tokenBase().claim("aud", "id-de-outro-aplicativo.apps.googleusercontent.com").build();

        assertThat(validar(deOutroApp).hasErrors())
                .as("F-03 em aberto: sem validacao de aud, token de qualquer app Google passa")
                .isFalse();
    }
}
