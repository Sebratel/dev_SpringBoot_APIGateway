package br.com.sebratel.bff.controller.dho;

import br.com.sebratel.bff.dto.ApiResponse;
import br.com.sebratel.bff.service.dho.DhoSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dho/settings")
@RequiredArgsConstructor
@Tag(name = "DHO Settings", description = "Endpoints for DHO configuration tables")
public class DhoSettingsController {

    private final DhoSettingsService service;

    @GetMapping
    @Operation(summary = "List all DHO settings (all tables)")
    public ResponseEntity<ApiResponse<Map<String, List<?>>>> getAllSettings() {
        return ResponseEntity.ok(ApiResponse.<Map<String, List<?>>>builder()
                .success(true)
                .message("Settings successfully retrieved.")
                .data(service.getAllSettings())
                .build());
    }

    @GetMapping("/base-origins")
    public ResponseEntity<ApiResponse<List<?>>> getBaseOrigins() {
        return ResponseEntity.ok(ApiResponse.<List<?>>builder()
                .success(true)
                .message("Base origins successfully retrieved.")
                .data(service.findAllBaseOrigins())
                .build());
    }

    @GetMapping("/departaments")
    public ResponseEntity<ApiResponse<List<?>>> getDepartaments() {
        return ResponseEntity.ok(ApiResponse.<List<?>>builder()
                .success(true)
                .message("Departaments successfully retrieved.")
                .data(service.findAllDepartaments())
                .build());
    }

    @GetMapping("/positions")
    public ResponseEntity<ApiResponse<List<?>>> getPositions() {
        return ResponseEntity.ok(ApiResponse.<List<?>>builder()
                .success(true)
                .message("Positions successfully retrieved.")
                .data(service.findAllPositions())
                .build());
    }

    @GetMapping("/teams")
    public ResponseEntity<ApiResponse<List<?>>> getTeams() {
        return ResponseEntity.ok(ApiResponse.<List<?>>builder()
                .success(true)
                .message("Teams successfully retrieved.")
                .data(service.findAllTeams())
                .build());
    }

    @GetMapping("/statuses")
    public ResponseEntity<ApiResponse<List<?>>> getOpportunityStatuses() {
        return ResponseEntity.ok(ApiResponse.<List<?>>builder()
                .success(true)
                .message("Opportunity statuses successfully retrieved.")
                .data(service.findAllOpportunityStatuses())
                .build());
    }
}
