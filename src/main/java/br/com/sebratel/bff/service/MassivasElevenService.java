package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.ConfirmacaoEllevenDTO;
import br.com.sebratel.bff.dto.CriacaoDeMassivaInputDTO;
import br.com.sebratel.bff.dto.CriacaoDeMassivaOutputDTO;
import br.com.sebratel.bff.dto.MassivaCriadaOutputDTO;
import br.com.sebratel.bff.enums.VoalleHeaderEnums;
import br.com.sebratel.bff.exceptions.IntegrationEllevenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Slf4j
@Service
public class MassivasElevenService {

    private final WebClient webClient;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Autowired
    public MassivasElevenService(WebClient webClient) {
        this.webClient = webClient;
    }

    public CriacaoDeMassivaOutputDTO salvarNoBancoERP(CriacaoDeMassivaInputDTO input) {
        log.info("Iniciando processo de criação de massiva no ERP. Data: {}, Hora: {}", input.getStartDate(), input.getStartTime());

        // 1. Criação inicial
        MassivaCriadaOutputDTO massivaCriada = this.criarNovaMassivaNoBanco(input.getStartDate(), input.getStartTime());
        String massivaId = massivaCriada.getId();
        log.info("Massiva criada com sucesso. ID Gerado: {}", massivaId);

        // 2. Pontos de impacto
        log.debug("Configurando pontos de impacto para a massiva ID: {}", massivaId);
        ConfirmacaoEllevenDTO confImpacto = this.setarPontosDeImpacto(
                Integer.valueOf(massivaId),
                input.getAccessPointIds(),
                input.getSlotOlt(),
                input.getPortaOlt(),
                input.getAddressListId()
        );
        validarResposta(confImpacto, "Adição de pontos de impacto");

        // 3. Conclusão e dados adicionais
        log.debug("Finalizando configuração de dados adicionais e notificações para ID: {}", massivaId);
        ConfirmacaoEllevenDTO confFinal = this.setarLugarDataEDadosAdicionais(massivaId, input);
        validarResposta(confFinal, "Finalização da massiva");

        log.info("Processo de criação de massiva ID: {} concluído com sucesso.", massivaId);

        // Mantendo a lógica original de retorno null conforme solicitado
        return null;
    }

    private void validarResposta(ConfirmacaoEllevenDTO resposta, String etapa) {
        if (resposta == null || !"success".equalsIgnoreCase(resposta.getSuccess())) {
            String detalhe = (resposta != null) ? resposta.getSuccess() : "Corpo da resposta nulo";
            log.error("Falha na etapa de integração: {}. Detalhe: {}", etapa, detalhe);
            throw new IntegrationEllevenException(String.format("Erro no Elleven em '%s': %s", etapa, detalhe));
        }
    }

    private MassivaCriadaOutputDTO criarNovaMassivaNoBanco(LocalDate data, LocalTime hora) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("id", "");
        formData.add("start_date", data.format(DATE_FORMATTER));
        formData.add("start_time", hora.format(TIME_FORMATTER));

        return webClient.post()
                .uri("/massive_incidents/createOrUpdateMassive")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(VoalleHeaderEnums.X_REQUESTED_WITH, VoalleHeaderEnums.XML_HTTP_REQUEST)
                .bodyValue(formData)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> handleHttpError("criação inicial", response))
                .bodyToMono(MassivaCriadaOutputDTO.class)
                .block();
    }

    private ConfirmacaoEllevenDTO setarPontosDeImpacto(Integer id, Integer[] apIds, Integer[] slots, Integer[] ports, Integer[] addrs) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("massive_incident_id", id.toString());

        addListToForm(formData, "access_point_ids[]", apIds);
        addListToForm(formData, "slot_olt[]", slots);
        addListToForm(formData, "port_olt[]", ports);
        addListToForm(formData, "address_list_id[]", addrs);

        return webClient.post()
                .uri("/massive_incidents/saveMassiveConnections")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(VoalleHeaderEnums.X_REQUESTED_WITH, VoalleHeaderEnums.XML_HTTP_REQUEST)
                .bodyValue(formData)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> handleHttpError("configuração de conexões", response))
                .bodyToMono(ConfirmacaoEllevenDTO.class)
                .block();
    }

    private ConfirmacaoEllevenDTO setarLugarDataEDadosAdicionais(String id, CriacaoDeMassivaInputDTO input) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        formData.add("company_place_id", String.valueOf(input.getCompanyPlaceId()));
        formData.add("assignment_type_id", String.valueOf(input.getAssignmentTypeId()));
        formData.add("assignment_description", input.getAssignmentDescription());
        formData.add("maintenance_date", input.getMaintenanceDate().format(DATE_FORMATTER));
        formData.add("maintenance_time", input.getMaintenanceTime().format(TIME_FORMATTER));
        formData.add("send_email", input.getSendEmail().toString());
        formData.add("send_sms", String.valueOf(input.getSendSms()));
        formData.add("send_push", String.valueOf(input.getSendPush()));
        formData.add("email_model_id", String.valueOf(input.getEmailModelId()));
        formData.add("return_email_model_id", String.valueOf(input.getReturnEmailModelId()));
        formData.add("push_model_id", String.valueOf(input.getPushModelId()));
        formData.add("return_push_model_id", String.valueOf(input.getReturnPushModelId()));

        addListToForm(formData, "access_point_ids[]", input.getAccessPointIds());

        return webClient.post()
                .uri(uri -> uri.path("/massive_incidents/concludeMassiveIncident/{id}").build(id))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(VoalleHeaderEnums.X_REQUESTED_WITH, VoalleHeaderEnums.XML_HTTP_REQUEST)
                .bodyValue(formData)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> handleHttpError("conclusão", response))
                .bodyToMono(ConfirmacaoEllevenDTO.class)
                .block();
    }

    // --- MÉTODOS AUXILIARES PARA LIMPEZA E REUSO ---

    private void addListToForm(MultiValueMap<String, String> form, String key, Integer[] values) {
        if (values != null) {
            Arrays.stream(values).forEach(v -> form.add(key, String.valueOf(v)));
        }
    }

    private Mono<? extends Throwable> handleHttpError(String context, org.springframework.web.reactive.function.client.ClientResponse response) {
        return response.bodyToMono(String.class)
                .flatMap(body -> {
                    log.error("Erro HTTP na etapa {}: Status {} - Body: {}", context, response.statusCode(), body);
                    return Mono.error(new RuntimeException("Falha na integração Elleven: " + body));
                });
    }
}