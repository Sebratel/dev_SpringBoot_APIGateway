package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.ApiResponse;
import br.com.sebratel.bff.dto.massivas.CriacaoDeMassivaInputDTO;
import br.com.sebratel.bff.dto.massivas.CriacaoDeMassivaOutputDTO;
import br.com.sebratel.bff.dto.massivas.EllevenApiResponseDTO;
import br.com.sebratel.bff.dto.massivas.MassivasBFFOutputDTO;
import br.com.sebratel.bff.dto.massivas.api.AberturaRegistroMassivoInputDTO;
import br.com.sebratel.bff.dto.massivas.api.AberturaRegistroMassivoOutputDTO;
import br.com.sebratel.bff.service.RecuperarTodasAsMassivasPeloBancoService;
import br.com.sebratel.bff.service.massivas.AdicionarMassivaNoEllevenApiService;
import br.com.sebratel.bff.service.massivas.AdicionarMassivaNoEllevenService;
import br.com.sebratel.bff.service.massivas.EnviarListaDeAfetadosParaNativeService;
import br.com.sebratel.bff.service.massivas.GetAllMassivesService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/massivas")
@Slf4j
public class MassivasElevenController {

    private final AdicionarMassivaNoEllevenService adicionarMassivaNoEllevenService;
    private final AdicionarMassivaNoEllevenApiService adicionarMassivaNoEllevenApiService;
    private final GetAllMassivesService getAllMassivesService;
    private final RecuperarTodasAsMassivasPeloBancoService recuperarTodasAsMassivasPeloBancoService;

    @Autowired
    public MassivasElevenController(AdicionarMassivaNoEllevenService adicionarMassivaNoEllevenService, AdicionarMassivaNoEllevenApiService adicionarMassivaNoEllevenApiService,
                                    EnviarListaDeAfetadosParaNativeService enviarListaDeAfetadosParaNativeService,
                                    GetAllMassivesService getAllMassivesService, RecuperarTodasAsMassivasPeloBancoService recuperarTodasAsMassivasPeloBancoService) {
        this.adicionarMassivaNoEllevenService = adicionarMassivaNoEllevenService;
        this.adicionarMassivaNoEllevenApiService = adicionarMassivaNoEllevenApiService;
        this.getAllMassivesService = getAllMassivesService;
        this.recuperarTodasAsMassivasPeloBancoService = recuperarTodasAsMassivasPeloBancoService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CriacaoDeMassivaOutputDTO>> criarMassivaComDadosDoFlutter(
            @Valid @RequestBody CriacaoDeMassivaInputDTO input) {

        log.info("Iniciando criação de massiva no ERP. [Solicitante: {}]", input.getAssignmentDescription());

        try {
            CriacaoDeMassivaOutputDTO output = adicionarMassivaNoEllevenService.salvarNoBancoERP(input);

            log.info("Massiva criada com sucesso no ERP. [ID Massiva: {}]", output.getId());

            ApiResponse<CriacaoDeMassivaOutputDTO> response = ApiResponse.<CriacaoDeMassivaOutputDTO>builder()
                    .success(true)
                    .message("Massiva criada com sucesso no ERP.")
                    .data(output)
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Erro ao criar massiva no ERP para o solicitante {}: {}", input.getAssignmentDescription(), e.getMessage());
            throw e;
        }
    }

    @GetMapping("/getAllMassives")
    public ResponseEntity<ApiResponse<EllevenApiResponseDTO>> recuperarTodasAsMassivas() {

        log.debug("Solicitando recuperação de todas as massivas do sistema Elleven.");

        EllevenApiResponseDTO output = getAllMassivesService.getAllSolicitations();

        int totalRecuperado = (output.getResponse() != null) ? output.getResponse().getTotalRecords() : 0;
        log.info("Recuperação de massivas finalizada. [Total encontrado: {}]", totalRecuperado);

        ApiResponse<EllevenApiResponseDTO> response = ApiResponse.<EllevenApiResponseDTO>builder()
                .success(true)
                .message("Massivas recuperadas com sucesso")
                .data(output)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/recuperar-pelo-banco")
    public ResponseEntity<ApiResponse<List<MassivasBFFOutputDTO>>> recuperarTodasAsMassivasPeloBanco() {
        try {
            List<MassivasBFFOutputDTO> output = recuperarTodasAsMassivasPeloBancoService.executar();

            log.info("Massivas recuperadas com sucesso no banco ERP.");

            ApiResponse<List<MassivasBFFOutputDTO>> response = ApiResponse.<List<MassivasBFFOutputDTO>>builder()
                    .success(true)
                    .message("Massivas recuperadas com sucesso.")
                    .data(output)
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            log.error("Erro ao recuperar massivas no banco ERP. {}", e.getMessage());
            throw e;
        }
    }

    @PostMapping("/salvar-massiva-via-api")
    public ResponseEntity<ApiResponse<AberturaRegistroMassivoOutputDTO>> criarMassivaComDadosDoFlutter(
            @Valid @RequestBody AberturaRegistroMassivoInputDTO input) {

        log.info("Iniciando criação de massiva no ERP via API. [Solicitante: {}]", input.getPersonId());

        try {
            AberturaRegistroMassivoOutputDTO output = adicionarMassivaNoEllevenApiService.executar(input);

            log.info("Massiva criada com sucesso no ERP. [ID PROTOCOLO: {}, ID ASSIGNMENT: {}, MESSAGE: {}]",
                    output.getResponse().getProtocol(),
                    output.getResponse().getAssignmentId(),
                    output.getResponse().getMessage());


            ApiResponse<AberturaRegistroMassivoOutputDTO> response = ApiResponse.<AberturaRegistroMassivoOutputDTO>builder()
                    .success(true)
                    .message("Massiva criada com sucesso no ERP.")
                    .data(output)
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Erro ao criar massiva no ERP para o solicitante {}:\n {}", input.getPersonId(), e.getMessage());
            throw e;
        }
    }

}