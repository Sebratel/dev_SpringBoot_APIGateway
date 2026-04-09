package br.com.sebratel.bff.service.massivas;

import br.com.sebratel.bff.dto.massivas.api.AberturaRegistroMassivoInputDTO;
import br.com.sebratel.bff.dto.massivas.api.AberturaRegistroMassivoOutputDTO;
import br.com.sebratel.bff.model.Employee;
import br.com.sebratel.bff.model.entity.AffectedUsersEntity;
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

    public static final int NORMAL_EVENT_INCIDENT_TYPE_ID = 1265;
    public static final int NORMAL_EVENT_CATALOG_SERVICE_ID = 1179;
    public static final String NORMAL_EVENT_CATEGORY_SOCILITATION = "MASSIVAS - 002";
    public static final int MASSIVE_EVENT_INCIDENT_TYPE_ID = 1257;
    public static final int MASSIVE_EVENT_CATALOG_SERVICE_ID = 1173;
    public static final String MASSIVE_EVENT_CATEGORY_SOCILITATION = "MASSIVAS - 001";

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


            input = this.decideForIncidentOrMassiveEvent(input);

            log.info("[MASSIVA] Regra de negócio aplicada: IncidentTypeId definido como {}",
                    input.getIncidentTypeId());

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

    private AberturaRegistroMassivoInputDTO decideForIncidentOrMassiveEvent(AberturaRegistroMassivoInputDTO input) {

        log.debug("[MASSIVA] Analisando contratos para identificar presença de B2B...");
        boolean hasB2B = employeeService.hasB2BinInput(input.getAffectedUsers().stream().map(AffectedUsersEntity::getContractId).toList());
        boolean isMassiveEvent = hasB2B || input.getAffectedUsersQuantity() > 15;
        if(isMassiveEvent){
            log.debug("Critérios atendidos para Evento Massivo (TITLE: {})", input.getAssignment().getTitle());
            input.setIncidentTypeId(MASSIVE_EVENT_INCIDENT_TYPE_ID);
            input.setSolicitationServiceCategory1(MASSIVE_EVENT_CATEGORY_SOCILITATION);
            input.setCatalogServiceId(MASSIVE_EVENT_CATALOG_SERVICE_ID);
            return input;
        }

        input.setIncidentTypeId(NORMAL_EVENT_INCIDENT_TYPE_ID);
        input.setSolicitationServiceCategory1(NORMAL_EVENT_CATEGORY_SOCILITATION);
        input.setCatalogServiceId(NORMAL_EVENT_CATALOG_SERVICE_ID);
        return input;
    }
}