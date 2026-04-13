package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.ApiResponse;
import br.com.sebratel.bff.dto.DhoSettingDTO;
import br.com.sebratel.bff.service.DhoSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/dho-settings")
@RequiredArgsConstructor
@Tag(name = "DHO Settings", description = "Endpoints for managing DHO settings")
public class DhoSettingController {

    private final DhoSettingService service;

    @GetMapping
    @Operation(summary = "List all DHO settings")
    public ResponseEntity<ApiResponse<List<DhoSettingDTO>>> findAll() {
        log.info("Received request to list DHO settings");
        try {
            List<DhoSettingDTO> settings = service.findAll();
            ApiResponse<List<DhoSettingDTO>> response = ApiResponse.<List<DhoSettingDTO>>builder()
                    .success(true)
                    .message("Settings successfully found.")
                    .data(settings)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error searching for DHO settings: {}", e.getMessage(), e);
            ApiResponse<List<DhoSettingDTO>> response = ApiResponse.<List<DhoSettingDTO>>builder()
                    .success(false)
                    .message("Error searching for settings: " + e.getMessage())
                    .build();
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
