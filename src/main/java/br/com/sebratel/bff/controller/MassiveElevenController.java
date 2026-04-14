package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.ApiResponse;
import br.com.sebratel.bff.utils.DatabaseErrorParser;

import br.com.sebratel.bff.dto.massivas.*;
import br.com.sebratel.bff.dto.massivas.api.AberturaRegistroMassivoInputDTO;
import br.com.sebratel.bff.dto.massivas.api.AberturaRegistroMassivoOutputDTO;
import br.com.sebratel.bff.dto.massivas.api.FinalizaRegistroMassivoInputDTO;
import br.com.sebratel.bff.dto.massivas.api.FinalizarRegistroMassivoOutputDTO;
import br.com.sebratel.bff.service.massivas.FinalizarMassivaNoEllevenApiService;
import br.com.sebratel.bff.service.massivas.RecuperarTodasAsMassivasPeloBancoService;
import br.com.sebratel.bff.service.massivas.AdicionarMassivaNoEllevenApiService;
import br.com.sebratel.bff.service.massivas.AdicionarMassivaNoEllevenService;
import br.com.sebratel.bff.service.massivas.EnviarListaDeAfetadosParaNativeService;
import br.com.sebratel.bff.service.massivas.GetAllMassivesService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/massive-incidents", "/api/v1/massivas-eleven", "/api/v1/massivas"})
@Slf4j
public class MassiveElevenController {

    private final AdicionarMassivaNoEllevenService adicionarMassivaNoEllevenService;
    private final AdicionarMassivaNoEllevenApiService adicionarMassivaNoEllevenApiService;
    private final GetAllMassivesService getAllMassivesService;
    private final RecuperarTodasAsMassivasPeloBancoService recuperarTodasAsMassivasPeloBancoService;
    private final FinalizarMassivaNoEllevenApiService finalizarMassivaNoEllevenApiService;

    @Autowired
    public MassiveElevenController(AdicionarMassivaNoEllevenService adicionarMassivaNoEllevenService, AdicionarMassivaNoEllevenApiService adicionarMassivaNoEllevenApiService,
                                    EnviarListaDeAfetadosParaNativeService enviarListaDeAfetadosParaNativeService,
                                    GetAllMassivesService getAllMassivesService, RecuperarTodasAsMassivasPeloBancoService recuperarTodasAsMassivasPeloBancoService, FinalizarMassivaNoEllevenApiService finalizarMassivaNoEllevenApiService) {
        this.adicionarMassivaNoEllevenService = adicionarMassivaNoEllevenService;
        this.adicionarMassivaNoEllevenApiService = adicionarMassivaNoEllevenApiService;
        this.getAllMassivesService = getAllMassivesService;
        this.recuperarTodasAsMassivasPeloBancoService = recuperarTodasAsMassivasPeloBancoService;
        this.finalizarMassivaNoEllevenApiService = finalizarMassivaNoEllevenApiService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CriacaoDeMassivaOutputDTO>> createMassiveIncidentWithFlutterData(
            @Valid @RequestBody CriacaoDeMassivaInputDTO input) {

        log.info("Starting massive incident creation in ERP. [Requester: {}]", input.getAssignmentDescription());

        try {
            CriacaoDeMassivaOutputDTO output = adicionarMassivaNoEllevenService.salvarNoBancoERP(input);

            log.info("Massive incident successfully created in ERP. [Massive ID: {}]", output.getId());

            ApiResponse<CriacaoDeMassivaOutputDTO> response = ApiResponse.<CriacaoDeMassivaOutputDTO>builder()
                    .success(true)
                    .message("Massive incident successfully created in ERP.")
                    .data(output)
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating massive incident in ERP for requester {}: {}", input.getAssignmentDescription(), e.getMessage());
            throw e;
        }
    }

    @GetMapping("/getAllMassives")
    public ResponseEntity<ApiResponse<EllevenApiResponseDTO>> retrieveAllMassiveIncidents() {

        log.debug("Requesting retrieval of all massive incidents from Elleven system.");

        EllevenApiResponseDTO output = getAllMassivesService.getAllSolicitations();

        int totalRetrieved = (output.getResponse() != null) ? output.getResponse().getTotalRecords() : 0;
        log.info("Massive incident retrieval finished. [Total found: {}]", totalRetrieved);

        ApiResponse<EllevenApiResponseDTO> response = ApiResponse.<EllevenApiResponseDTO>builder()
                .success(true)
                .message("Massive incidents successfully retrieved")
                .data(output)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping({"/recover-via-database", "/recuperar-pelo-banco"})
    public ResponseEntity<ApiResponse<List<MassivasBFFOutputDTO>>> retrieveAllMassiveIncidentsViaDatabase() {
        try {
            List<MassivasBFFOutputDTO> output = recuperarTodasAsMassivasPeloBancoService.executar();

            log.info("Massive incidents successfully retrieved from ERP database.");

            ApiResponse<List<MassivasBFFOutputDTO>> response = ApiResponse.<List<MassivasBFFOutputDTO>>builder()
                    .success(true)
                    .message("Massive incidents successfully retrieved.")
                    .data(output)
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            log.error("Error retrieving massive incidents from ERP database. {}", e.getMessage());
            throw e;
        }
    }

    @PostMapping({"/save-massive-via-api", "salvar-massiva-via-api"})
    public ResponseEntity<ApiResponse<AberturaRegistroMassivoOutputDTO>> createMassiveIncidentViaApi(
            @Valid @RequestBody AberturaRegistroMassivoInputDTO input) {

        log.info("Starting massive incident creation in ERP via API. [Requester: {}]", input.getPersonId());

        try {
            AberturaRegistroMassivoOutputDTO output = adicionarMassivaNoEllevenApiService.executar(input);

            if(!output.isSuccess()) {
                ApiResponse<AberturaRegistroMassivoOutputDTO> apiResponse = ApiResponse.<AberturaRegistroMassivoOutputDTO>builder().success(false).message("Failed to create massive incident in ERP. Elleven side ERROR").data(output).build();
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(apiResponse);
            }

            log.info("Massive incident successfully created in ERP. [PROTOCOL ID: {}, ASSIGNMENT ID: {}, MESSAGE: {}]",
                    output.getResponse().getProtocol(),
                    output.getResponse().getAssignmentId(),
                    output.getResponse().getMessage());


            ApiResponse<AberturaRegistroMassivoOutputDTO> response = ApiResponse.<AberturaRegistroMassivoOutputDTO>builder()
                    .success(true)
                    .message("Massive incident successfully created in ERP.")
                    .data(output)
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating massive incident in ERP for requester {}:\n {}", input.getPersonId(), e.getMessage());
            throw e;
        }
    }

    @DeleteMapping({"/finalize-ticket-via-api", "finalizar-chamado-via-api"})
    public ResponseEntity<FinalizarRegistroMassivoOutputDTO> finalizeMassiveIncidentViaApi(
            @Valid @RequestBody FinalizaRegistroMassivoInputDTO input) {

        log.info("Starting massive incident finalization in ERP via API.");

        try {
            FinalizarRegistroMassivoOutputDTO finalizarRegistroMassivoOutputDTO = finalizarMassivaNoEllevenApiService.executar(input);

            if(finalizarRegistroMassivoOutputDTO.isSuccess()) {
                log.info("Massive incident successfully finalized in ERP. [ASSIGNMENT ID: {}, MESSAGE: {}]",
                        input.getAssignmentId(),
                        input.getDescription());

                return ResponseEntity.ok(finalizarRegistroMassivoOutputDTO);

            } else {
                log.error("Error finalizing massive incident in ERP. [ASSIGNMENT ID: {}, MESSAGE: {}]",
                        input.getAssignmentId(),
                        input.getDescription());

                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(finalizarRegistroMassivoOutputDTO);
            }
        } catch (Exception e) {
            log.error("Error finalizing massive incident SERVER ERROR. ASSIGNMENT: {}:\n {}", input.getAssignmentId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @CacheEvict("token-de-integracao")
    @PostMapping("/invalidate-token")
    public void tokenCacheEvict() {
        log.info("Integration token removed from cache");
    }


}
