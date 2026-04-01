package br.com.sebratel.bff.service.massivas;

import br.com.sebratel.bff.dto.massivas.api.AberturaRegistroMassivoInputDTO;
import br.com.sebratel.bff.dto.massivas.api.AberturaRegistroMassivoOutputDTO;
import br.com.sebratel.bff.model.entity.UsuarioAfetadoEntity;
import br.com.sebratel.bff.service.EmployeeService;
import br.com.sebratel.bff.service.RecuperarTokenDoUsuarioIntegradorEllevenService;
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
        log.info("Iniciando processo de abertura de registro massivo no Elleven para o título: {}", input.getAssignment().getTitle());

        String token = recuperarTokenDoUsuarioIntegradorEllevenService.executar().accessToken();
        String url = "https://erp.sebratel.net.br:45715/external/integrations/thirdparty/opendetailedsolicitation";

        log.debug("Verificando se existem contratos B2B entre os usuários afetados...");
        boolean hasB2B = employeeService.hasB2BinInput(input.getAffectedUsers().stream().map(UsuarioAfetadoEntity::getContractId).toList());

        int incidentTypeId = this.decideForIncidentOrMassiveEvent(input, hasB2B);
        input.setIncidentTypeId(incidentTypeId);

        log.info("Tipo de incidente decidido: {} (Baseado em hasB2B: {} e Qtd Afetados: {})",
                incidentTypeId, hasB2B, input.getAffectedUsersQuantity());

        try {
            log.info("Enviando requisição POST para a API Elleven em: {}", url);
            AberturaRegistroMassivoOutputDTO response = webClient
                    .post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(input)
                    .retrieve()
                    .bodyToMono(AberturaRegistroMassivoOutputDTO.class)
                    .block();

            log.info("Registro massivo criado com sucesso no Elleven. Resposta: {}", response);
            return response;

        } catch (WebClientResponseException.Unauthorized e) {
            log.error("Erro 401 (Não autorizado) ao tentar acessar a API do Elleven.");
            if (cacheManager.getCache("token-de-integracao") != null) {
                log.warn("Limpando cache 'token-de-integracao' devido a expiração ou invalidade do token.");
                Objects.requireNonNull(cacheManager.getCache("token-de-integracao")).evict("token-static-key");
            }
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao processar abertura de massiva no Elleven: {}", e.getMessage(), e);
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