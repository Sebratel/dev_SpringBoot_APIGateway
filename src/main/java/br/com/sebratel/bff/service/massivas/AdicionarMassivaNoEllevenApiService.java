package br.com.sebratel.bff.service.massivas;

import br.com.sebratel.bff.dto.massivas.api.AberturaRegistroMassivoInputDTO;
import br.com.sebratel.bff.dto.massivas.api.AberturaRegistroMassivoOutputDTO;
import br.com.sebratel.bff.model.Employee;
import br.com.sebratel.bff.model.entity.UsuarioAfetadoEntity;
import br.com.sebratel.bff.service.EmployeeService;
import br.com.sebratel.bff.service.RecuperarTokenDoUsuarioIntegradorEllevenService;
import br.com.sebratel.bff.utils.JwtInformation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Objects;

@Service
@Slf4j
public class AdicionarMassivaNoEllevenApiService {

    public static final int ID_FOR_NORMAL_INCIDENT = 1265;
    public static final int ID_FOR_MASSIVE_INCIDENT = 1257;
    private final RecuperarTokenDoUsuarioIntegradorEllevenService recuperarTokenDoUsuarioIntegradorEllevenService;
    private final WebClient webClient;
    private final CacheManager cacheManager;
    private final EmployeeService employeeService;

    @Autowired
    public AdicionarMassivaNoEllevenApiService(RecuperarTokenDoUsuarioIntegradorEllevenService recuperarTokenDoUsuarioIntegradorEllevenService, WebClient webClient, CacheManager cacheManager, EmployeeService employeeService) {
        this.recuperarTokenDoUsuarioIntegradorEllevenService = recuperarTokenDoUsuarioIntegradorEllevenService;
        this.webClient = webClient;
        this.cacheManager = cacheManager;
        this.employeeService = employeeService;
    }

    public AberturaRegistroMassivoOutputDTO executar(@Valid AberturaRegistroMassivoInputDTO input) {
        Employee x = JwtInformation.retrieveUserData();
        String email = x.email();
        String name = x.name();

        input.getAssignment().setDescription(input.getAssignment().getDescription() + " - " + name + "("+ email +")");

        log.info("[MASSIVA] Usuário {} ({}) solicitando abertura de registro: '{}'. Total de usuários afetados: {}",
                name, email, input.getAssignment().getTitle(), input.getAffectedUsersQuantity());

        try {
            log.debug("[MASSIVA] Buscando token de integração...");
            String token = recuperarTokenDoUsuarioIntegradorEllevenService.executar().accessToken();
            String url = "https://erp.sebratel.net.br:45715/external/integrations/thirdparty/opendetailedsolicitation";

            log.debug("[MASSIVA] Analisando contratos para identificar presença de B2B...");
            boolean hasB2B = employeeService.hasB2BinInput(input.getAffectedUsers().stream().map(UsuarioAfetadoEntity::getContractId).toList());

            int incidentTypeId = this.decideForIncidentOrMassiveEvent(input, hasB2B);
            input.setIncidentTypeId(incidentTypeId);

            log.info("[MASSIVA] Regra de negócio aplicada: IncidentTypeId definido como {} (Possui B2B: {})",
                    incidentTypeId, hasB2B);

            log.info("[MASSIVA] Enviando payload para Elleven API: {}", url);

            // Medindo o tempo de resposta da API externa (opcional, mas muito útil)
            long startTime = System.currentTimeMillis();

            AberturaRegistroMassivoOutputDTO response = webClient
                    .post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(input)
                    .retrieve()
                    .bodyToMono(AberturaRegistroMassivoOutputDTO.class)
                    .block();

            long duration = System.currentTimeMillis() - startTime;

            log.info("[MASSIVA] Sucesso! Registro criado no Elleven em {}ms. Resposta: {}", duration, response);
            return response;

        } catch (WebClientResponseException.Unauthorized e) {
            log.error("[MASSIVA-ERRO] Erro de autenticação (401) na API Elleven. Verificando limpeza de cache.");
            if (cacheManager.getCache("token-de-integracao") != null) {
                log.warn("[MASSIVA-CACHE] Token inválido detectado. Evicting 'token-static-key' do cache.");
                Objects.requireNonNull(cacheManager.getCache("token-de-integracao")).evict("token-static-key");
            }
            throw e;
        } catch (WebClientResponseException e) {
            // Log específico para erros de API (4xx ou 5xx) com o corpo do erro da API
            log.error("[MASSIVA-ERRO] Falha na API Elleven. Status: {}. Response Body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            log.error("[MASSIVA-ERRO] Falha crítica ao processar massiva. Título: {}. Causa: {}",
                    input.getAssignment().getTitle(), e.getMessage(), e);
            throw e;
        }
    }

    private int decideForIncidentOrMassiveEvent(AberturaRegistroMassivoInputDTO input, boolean hasB2B) {
        if(!hasB2B && input.getAffectedUsersQuantity() <= 15){
            log.debug("Critérios atendidos para Incidente Normal (ID: {})", ID_FOR_NORMAL_INCIDENT);
            return ID_FOR_NORMAL_INCIDENT;
        }
        log.debug("Critérios atendidos para Evento Massivo (ID: {})", ID_FOR_MASSIVE_INCIDENT);
        return ID_FOR_MASSIVE_INCIDENT;
    }
}