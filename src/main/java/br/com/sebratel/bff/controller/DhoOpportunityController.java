package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.ApiResponse;
import br.com.sebratel.bff.dto.DhoOpportunityDTO;
import br.com.sebratel.bff.service.DhoOpportunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/dho-opportunities")
@RequiredArgsConstructor
@Tag(name = "DHO Opportunities", description = "Endpoints for managing active collaborators (DHO)")
public class DhoOpportunityController {

    private final DhoOpportunityService service;

    @GetMapping
    @Operation(summary = "List all active collaborators with optional status filter")
    public ResponseEntity<ApiResponse<List<DhoOpportunityDTO>>> findAll(
            @RequestParam(required = false) String status) {
        log.info("Received request to list active collaborators. Status filter: {}", status);
        try {
            List<DhoOpportunityDTO> opportunities;
            if (status != null && !status.isEmpty()) {
                opportunities = service.findByStatus(status);
            } else {
                opportunities = service.findAll();
            }

            ApiResponse<List<DhoOpportunityDTO>> response = ApiResponse.<List<DhoOpportunityDTO>>builder()
                    .success(true)
                    .message("Collaborators successfully found.")
                    .data(opportunities)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error searching for collaborators: {}", e.getMessage(), e);
            ApiResponse<List<DhoOpportunityDTO>> response = ApiResponse.<List<DhoOpportunityDTO>>builder()
                    .success(false)
                    .message("Error searching for collaborators: " + e.getMessage())
                    .build();
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
