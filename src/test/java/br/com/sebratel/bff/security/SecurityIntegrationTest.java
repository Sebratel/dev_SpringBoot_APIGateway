package br.com.sebratel.bff.security;

import br.com.sebratel.bff.config.SecurityConfig;
import br.com.sebratel.bff.controller.AffectedUserController;
import br.com.sebratel.bff.controller.MatrixController;
import br.com.sebratel.bff.dto.MatrixMassiveOutputDTO;
import br.com.sebratel.bff.dto.massivas.ImpactedUsersOutputDTO;
import br.com.sebratel.bff.service.AffectedUserService;
import br.com.sebratel.bff.service.MatrixService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Matriz de autenticacao e autorizacao executada com a cadeia de filtros de seguranca
 * <b>ligada</b>.
 *
 * <p>Motivo de existir (finding F-17): {@code BaseTest} usa
 * {@code @AutoConfigureMockMvc(addFilters = false)}, de modo que os 25 testes de
 * controller existentes nunca exercitam o {@code SecurityFilterChain}. Sem esta classe,
 * um {@code permitAll()} acidental ou a remocao de uma regra de autorizacao passariam
 * despercebidos com a suite inteira verde.
 *
 * <p>Esta classe e <b>aditiva</b>: nao altera o {@code BaseTest} nem os testes atuais.
 *
 * <p>O {@code OAuth2ClientAutoConfiguration} e excluido porque trata do fluxo de login
 * "Sign in with Google" (exige client-id preenchido), que nao participa da validacao de
 * token feita pelo resource server -- que e justamente o que esta classe cobre.
 *
 * <p>Alguns cenarios documentam o comportamento <b>atual</b>, incluindo pontos que a
 * auditoria classificou como problematicos: as rotas publicas (F-04/F-05) e o HTTP Basic
 * compartilhado (F-02). Sao registros deliberados do estado presente -- quando esses
 * findings forem corrigidos, estes testes devem falhar e ser atualizados junto, e esse e
 * exatamente o sinal de alerta que hoje nao existe.
 */
@WebMvcTest(controllers = {MatrixController.class, AffectedUserController.class})
@Import(SecurityConfig.class)
@ImportAutoConfiguration(exclude = OAuth2ClientAutoConfiguration.class)
@ActiveProfiles("test")
class SecurityIntegrationTest {

    /** Mesmas credenciais definidas em src/test/resources/application-test.properties. */
    private static final String BASIC_USER = "test-user";
    private static final String BASIC_PASS = "test-pass";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MatrixService matrixService;

    @MockitoBean
    private AffectedUserService affectedUserService;

    private static ImpactedUsersOutputDTO vazio() {
        return ImpactedUsersOutputDTO.builder().impactedUsers(List.of()).build();
    }

    // ---------------------------------------------------------------
    // Rotas protegidas
    // ---------------------------------------------------------------

    @Test
    @DisplayName("GET protegido sem nenhuma credencial deve retornar 401")
    void semCredencialDeveRetornar401() throws Exception {
        mockMvc.perform(get("/api/v1/afetados"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE destrutivo sem credencial deve retornar 401")
    void deleteSemCredencialDeveRetornar401() throws Exception {
        mockMvc.perform(delete("/api/v1/afetados/protocol/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST sem credencial deve retornar 401")
    void postSemCredencialDeveRetornar401() throws Exception {
        mockMvc.perform(post("/api/v1/afetados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Basic com senha errada deve retornar 401")
    void basicComSenhaErradaDeveRetornar401() throws Exception {
        mockMvc.perform(get("/api/v1/afetados").with(httpBasic(BASIC_USER, "senha-errada")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Basic com usuario inexistente deve retornar 401")
    void basicComUsuarioInexistenteDeveRetornar401() throws Exception {
        mockMvc.perform(get("/api/v1/afetados").with(httpBasic("nao-existe", BASIC_PASS)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Token Bearer malformado deve retornar 401")
    void tokenMalformadoDeveRetornar401() throws Exception {
        mockMvc.perform(get("/api/v1/afetados").header("Authorization", "Bearer nao-e-um-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("JWT valido deve passar pela cadeia de seguranca e chegar ao controller")
    void jwtValidoDeveSerAceito() throws Exception {
        when(affectedUserService.getAll()).thenReturn(vazio());

        // Lista vazia produz 404 de negocio no controller. O que importa aqui e que
        // NAO foi 401: a requisicao atravessou a cadeia de seguranca.
        mockMvc.perform(get("/api/v1/afetados")
                        .with(jwt().jwt(j -> j.claim("email", "alguem@sebratel.com.br"))))
                .andExpect(status().isNotFound());
    }

    // ---------------------------------------------------------------
    // Rotas publicas conhecidas -- comportamento atual (F-04 / F-05)
    // ---------------------------------------------------------------

    @Test
    @DisplayName("GET /api/v1/matrix responde sem autenticacao (F-04, comportamento atual)")
    void matrixEstaPublico() throws Exception {
        when(matrixService.getContractInfoByCPF(anyString()))
                .thenReturn(MatrixMassiveOutputDTO.builder().status("not_found_client").build());

        mockMvc.perform(get("/api/v1/matrix")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpf\":\"00000000000\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/afetados/contract/{id} responde sem autenticacao (F-05, atual)")
    void contratoEstaPublico() throws Exception {
        when(affectedUserService.getUsuariosAfetadosByContractId(anyLong())).thenReturn(vazio());

        mockMvc.perform(get("/api/v1/afetados/contract/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("O permitAll de /afetados/contract nao vaza para os aliases do controller")
    void aliasesDoControllerContinuamProtegidos() throws Exception {
        mockMvc.perform(get("/api/v1/impacted-users/contract/1"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/usuario-afetado/contract/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("O permitAll de /afetados vale apenas para GET em /contract/**")
    void permitAllNaoSeAplicaAOutrosCaminhos() throws Exception {
        mockMvc.perform(get("/api/v1/afetados/protocol/1"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/afetados/pppoe/qualquer"))
                .andExpect(status().isUnauthorized());
    }
}
