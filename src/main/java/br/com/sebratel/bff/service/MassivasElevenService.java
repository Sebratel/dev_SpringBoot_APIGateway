package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.ConfirmacaoEllevenDTO;
import br.com.sebratel.bff.dto.CriacaoDeMassivaInputDTO;
import br.com.sebratel.bff.dto.CriacaoDeMassivaOutputDTO;
import br.com.sebratel.bff.dto.MassivaCriadaOutputDTO;
import br.com.sebratel.bff.enums.VoalleHeaderEnums;
import br.com.sebratel.bff.exceptions.IntegrationEllevenException;
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

@Service
public class MassivasElevenService {

    private final WebClient webClient;

    @Autowired
    public MassivasElevenService(WebClient webClient) {
        this.webClient = webClient;
    }

    public CriacaoDeMassivaOutputDTO salvarNoBancoERP(CriacaoDeMassivaInputDTO input) {
        MassivaCriadaOutputDTO massivaCriadaOutputDTO = this.criarNovaMassivaNoBanco(input.getStartDate(), input.getStartTime());
        ConfirmacaoEllevenDTO confirmacaoDeSucessoParaPontoDeImpacto = this.setarPontosDeImpacto(Integer.valueOf(massivaCriadaOutputDTO.getId()), input.getAccessPointIds(), input.getSlotOlt(), input.getPortaOlt(), input.getAddressListId());

        validarResposta(confirmacaoDeSucessoParaPontoDeImpacto, "Adição de dados de ponto de impacto da massiva criada");

        ConfirmacaoEllevenDTO confirmacaoEllevenDTOparaSetDeDadosAdicionais = this.setarLugarDataEDadosAdicionais(
                massivaCriadaOutputDTO.getId(),
                input.getCompanyPlaceId(),
                input.getAssignmentTypeId(),
                input.getAssignmentDescription(),
                input.getMaintenanceDate(),
                input.getMaintenanceTime(),
                input.getSendEmail(),
                input.getSendSms(),
                input.getEmailModelId(),
                input.getReturnEmailModelId(),
                input.getAccessPointIds(),
                input.getSendPush(),
                input.getPushModelId(),
                input.getReturnPushModelId()
        );

        validarResposta(confirmacaoEllevenDTOparaSetDeDadosAdicionais, "Finalização da criação da massiva adicionando lugares e destinatários.");

        return null;
    }

    private void validarResposta(ConfirmacaoEllevenDTO resposta, String etapa) {
        if (resposta == null || !"success".equalsIgnoreCase(resposta.getSuccess())) {
            String detalhe = (resposta != null) ? resposta.getSuccess() : "Sem resposta do servidor";
            throw new IntegrationEllevenException(
                    String.format("Falha ao %s no Elleven. Detalhe: %s", etapa, detalhe)
            );
        }
    }

    private MassivaCriadaOutputDTO criarNovaMassivaNoBanco(
            LocalDate dataInicio,
            LocalTime horaInicio
    ) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        formData.add("id", "");
        formData.add("start_date", dataInicio.format(dateFormatter));
        formData.add("start_time", horaInicio.format(timeFormatter));

        return webClient.post()
                .uri("/massive_incidents/createOrUpdateMassive")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(VoalleHeaderEnums.X_REQUESTED_WITH, VoalleHeaderEnums.XML_HTTP_REQUEST)
                .bodyValue(formData)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class).flatMap(body ->
                                Mono.error(new RuntimeException("Falha no ERP: " + body)))
                )
                .bodyToMono(MassivaCriadaOutputDTO.class)
                .block();
    }

    private ConfirmacaoEllevenDTO setarPontosDeImpacto(Integer massiveIncidentId, Integer[] accessPointIds, Integer[] slotOlt, Integer[] portsOlt, Integer[] addressListId) {

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        formData.add("massive_incident_id", massiveIncidentId.toString());

        if (accessPointIds != null) {
            for (Integer id : accessPointIds) {
                formData.add("access_point_ids[]", id.toString());
            }
        }

        if (slotOlt != null) {
            for (Integer slot : slotOlt) {
                formData.add("slot_olt[]", slot.toString());
            }
        }

        if (portsOlt != null) {
            for (Integer porta : portsOlt) {
                formData.add("port_olt[]", porta.toString());
            }
        }

        if (addressListId != null) {
            for (Integer addr : addressListId) {
                formData.add("address_list_id[]", addr.toString());
            }
        }

        return webClient.post()
                .uri("/massive_incidents/saveMassiveConnections")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(VoalleHeaderEnums.X_REQUESTED_WITH, VoalleHeaderEnums.XML_HTTP_REQUEST) // Exigido pelo seu curl
                .bodyValue(formData)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .flatMap(body -> Mono.error(new RuntimeException("Erro Voalle: " + body)))
                )
                .bodyToMono(ConfirmacaoEllevenDTO.class)
                .block();
    }

    private ConfirmacaoEllevenDTO setarLugarDataEDadosAdicionais(
            String id,
            Integer companyPlaceId,
            Integer assignmentTypeId,
            String assignmentDescription,
            LocalDate maintenanceDate,
            LocalTime maintenanceTime,
            Integer sendEmail,
            Integer sendSms,
            Integer emailModelId,
            Integer returnEmailModelId,
            Integer[] acessPointIds,
            Integer sendPush,
            Integer pushModelId,
            Integer returnPushModelId) {

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        // Mapeamento dos campos fixos e strings
        formData.add("company_place_id", String.valueOf(companyPlaceId));
        formData.add("assignment_type_id", String.valueOf(assignmentTypeId));
        formData.add("assignment_description", assignmentDescription);
        formData.add("maintenance_date", maintenanceDate.format(dateFormatter));
        formData.add("maintenance_time", maintenanceTime.format(timeFormatter));
        formData.add("send_email", sendEmail.toString());
        formData.add("send_sms", String.valueOf(sendSms));
        formData.add("send_push", String.valueOf(sendPush));

        // Campos vazios obrigatórios pelo seu curl
        formData.add("email_model_id", String.valueOf(emailModelId));
        formData.add("return_email_model_id", String.valueOf(returnEmailModelId));
        formData.add("push_model_id", String.valueOf(pushModelId));
        formData.add("return_push_model_id", String.valueOf(returnPushModelId));

        // Array de access points com colchetes
        if (acessPointIds != null) {
            for (Integer idAcessPoint : acessPointIds) {
                formData.add("access_point_ids[]", String.valueOf(idAcessPoint));
            }
        }

        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/massive_incidents/concludeMassiveIncident/{id}")
                        .build(id))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(VoalleHeaderEnums.X_REQUESTED_WITH, VoalleHeaderEnums.XML_HTTP_REQUEST)
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(ConfirmacaoEllevenDTO.class)
                .block();


    }
}
