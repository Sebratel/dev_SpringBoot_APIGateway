package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.ApiResponse;
import br.com.sebratel.bff.dto.massivas.api.AberturaRegistroMassivoInputDTO;
import br.com.sebratel.bff.dto.massivas.api.AberturaRegistroMassivoOutputDTO;
import br.com.sebratel.bff.service.gmud.AbrirGmudNoEllevenApiService;
import br.com.sebratel.bff.service.massivas.MassivaIdempotencyStore;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Abertura de GMUD (Gestão de Mudança de Rede) no Elleven. Endpoint próprio (separado da massiva)
 * porque a classificação não pode ser sobrescrita pela regra da massiva. Reusa a idempotência
 * ({@link MassivaIdempotencyStore}) — retry após timeout não duplica o protocolo.
 */
@RestController
@RequestMapping("/api/v1/gmud")
@Slf4j
public class GmudEllevenController {

    private final AbrirGmudNoEllevenApiService abrirGmudNoEllevenApiService;
    private final MassivaIdempotencyStore idempotencyStore;

    @Autowired
    public GmudEllevenController(
            AbrirGmudNoEllevenApiService abrirGmudNoEllevenApiService,
            MassivaIdempotencyStore idempotencyStore) {
        this.abrirGmudNoEllevenApiService = abrirGmudNoEllevenApiService;
        this.idempotencyStore = idempotencyStore;
    }

    @PostMapping({"/abrir-via-api", "open-via-api"})
    public ResponseEntity<ApiResponse<AberturaRegistroMassivoOutputDTO>> abrirGmudViaApi(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody AberturaRegistroMassivoInputDTO input) {
        return idempotencyStore.execute(idempotencyKey, () -> doAbrirGmudViaApi(input));
    }

    private ResponseEntity<ApiResponse<AberturaRegistroMassivoOutputDTO>> doAbrirGmudViaApi(
            AberturaRegistroMassivoInputDTO input) {

        log.info("Starting GMUD creation in ERP via API. [Requester: {}]", input.getPersonId());

        try {
            AberturaRegistroMassivoOutputDTO output = abrirGmudNoEllevenApiService.executar(input);

            if (!output.isSuccess()) {
                ApiResponse<AberturaRegistroMassivoOutputDTO> apiResponse =
                        ApiResponse.<AberturaRegistroMassivoOutputDTO>builder()
                                .success(false)
                                .message("Failed to create GMUD in ERP. Elleven side ERROR")
                                .data(output)
                                .build();
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(apiResponse);
            }

            log.info("GMUD successfully created in ERP. [PROTOCOL ID: {}, ASSIGNMENT ID: {}]",
                    output.getResponse().getProtocol(),
                    output.getResponse().getAssignmentId());

            ApiResponse<AberturaRegistroMassivoOutputDTO> response =
                    ApiResponse.<AberturaRegistroMassivoOutputDTO>builder()
                            .success(true)
                            .message("GMUD successfully created in ERP.")
                            .data(output)
                            .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating GMUD in ERP for requester {}: {}", input.getPersonId(), e.getMessage());
            throw e;
        }
    }
}
