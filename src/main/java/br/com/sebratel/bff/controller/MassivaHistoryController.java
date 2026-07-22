package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.ApiResponse;
import br.com.sebratel.bff.dto.massivas.MassivaHistoryOutputDTO;
import br.com.sebratel.bff.service.massivas.RecuperarTodasAsMassivasHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/massivas-history", "/api/v1/massivas-splitters"})
@Slf4j
public class MassivaHistoryController {

    private final RecuperarTodasAsMassivasHistoryService recuperarTodasAsMassivasHistoryService;

    @Autowired
    public MassivaHistoryController(RecuperarTodasAsMassivasHistoryService recuperarTodasAsMassivasHistoryService) {
        this.recuperarTodasAsMassivasHistoryService = recuperarTodasAsMassivasHistoryService;
    }

    @GetMapping({"/recover-via-database", "/recuperar-pelo-banco"})
    public ResponseEntity<ApiResponse<List<MassivaHistoryOutputDTO>>> retrieveAllMassivaHistoryViaDatabase() {
        try {
            List<MassivaHistoryOutputDTO> output = recuperarTodasAsMassivasHistoryService.executar();

            log.info("Massiva history successfully retrieved from splitters database.");

            ApiResponse<List<MassivaHistoryOutputDTO>> response = ApiResponse.<List<MassivaHistoryOutputDTO>>builder()
                    .success(true)
                    .message("Massiva history successfully retrieved.")
                    .data(output)
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            log.error("Error retrieving massiva history from splitters database. {}", e.getMessage());
            throw e;
        }
    }
}
