package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.ApiResponse;
import br.com.sebratel.bff.dto.massivas.*;
import br.com.sebratel.bff.service.RecuperarTodasAsMassivasPeloBancoService;
import br.com.sebratel.bff.service.massivas.AdicionarMassivaNoElevenService;
import br.com.sebratel.bff.service.massivas.EnviarListaDeAfetadosParaNativeService;
import br.com.sebratel.bff.service.massivas.GetAllMassivesService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/massivas")
@Slf4j
public class MassivasElevenController {

    private final AdicionarMassivaNoElevenService adicionarMassivaNoElevenService;
    private final EnviarListaDeAfetadosParaNativeService enviarListaDeAfetadosParaNativeService;
    private final GetAllMassivesService getAllMassivesService;
    private final RecuperarTodasAsMassivasPeloBancoService recuperarTodasAsMassivasPeloBancoService;

    @Autowired
    public MassivasElevenController(AdicionarMassivaNoElevenService adicionarMassivaNoElevenService,
                                    EnviarListaDeAfetadosParaNativeService enviarListaDeAfetadosParaNativeService,
                                    GetAllMassivesService getAllMassivesService, RecuperarTodasAsMassivasPeloBancoService recuperarTodasAsMassivasPeloBancoService) {
        this.adicionarMassivaNoElevenService = adicionarMassivaNoElevenService;
        this.enviarListaDeAfetadosParaNativeService = enviarListaDeAfetadosParaNativeService;
        this.getAllMassivesService = getAllMassivesService;
        this.recuperarTodasAsMassivasPeloBancoService = recuperarTodasAsMassivasPeloBancoService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CriacaoDeMassivaOutputDTO>> criarMassivaComDadosDoFlutter(
            @Valid @RequestBody CriacaoDeMassivaInputDTO input) {

        log.info("Iniciando criação de massiva no ERP. [Solicitante: {}]", input.getAssignmentDescription());

        try {
            CriacaoDeMassivaOutputDTO output = adicionarMassivaNoElevenService.salvarNoBancoERP(input);

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

    @GetMapping("/enviar-dados-para-native")
    public ResponseEntity<ApiResponse<ImpactedUsersDTO>> enviarListaDosAfetadosParaNative(
            @Valid @RequestBody ImpactedUsersDTO input) {

        int totalUsers = input.getImpactedUsers() != null ? input.getImpactedUsers().length : 0;
        log.info("Iniciando envio de lista de afetados para Native. [Total de usuários: {}]", totalUsers);

        try {
//            ImpactedUsersDTO output = enviarListaDeAfetadosParaNativeService.executar(input);
            ImpactedUsersDTO dadosDeTest = new ImpactedUsersDTO();
            Map<String, ImpactDetailsDTO> map = new HashMap<>();

            ImpactDetailsDTO impactDetailsDTO = new ImpactDetailsDTO();
            impactDetailsDTO.setReason("razao");
            impactDetailsDTO.setEstimateTimeOfRestoration(LocalDateTime.now());

            map.put("lidomarcantarelli253636", impactDetailsDTO);

// CORREÇÃO: Criar um array de Mapas e colocar o seu mapa dentro dele
            Map<String, ImpactDetailsDTO>[] mapaArray = new Map[]{map};

            dadosDeTest.setImpactedUsers(mapaArray);
            log.info("Envio para Native concluído com sucesso.");

            ApiResponse<ImpactedUsersDTO> response = ApiResponse.<ImpactedUsersDTO>builder()
                    .success(true)
                    .message("Usuarios listados enviados para native.")
                    .data(dadosDeTest)
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Falha no envio da lista de {} usuários para o Native: {}", totalUsers, e.getMessage());
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

            log.info("Massivas recuperadas com sucesso no banco ERP");

            ApiResponse<List<MassivasBFFOutputDTO>> response = ApiResponse.<List<MassivasBFFOutputDTO>>builder()
                    .success(true)
                    .message("Massiva criada com sucesso no ERP.")
                    .data(output)
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Erro ao recuperar massivas no banco ERP. {}", e.getMessage());
            throw e;
        }
    }

}