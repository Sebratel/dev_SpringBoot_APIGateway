package br.com.sebratel.bff.controller.dho;

import br.com.sebratel.bff.dto.ApiResponse;
import br.com.sebratel.bff.dto.DhoOpportunitiesDTO;
import br.com.sebratel.bff.service.dho.DhoOpportunitiesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping({"/api/v1/massivas/dho-opportunities", "/api/v1/dho/opportunities"})
@RequiredArgsConstructor
@Tag(name = "DHO Opportunities V2", description = "New endpoints for DHO opportunities")
public class DhoOpportunitiesController {
    private final DhoOpportunitiesService service;

    @GetMapping
    @Operation(summary = "List all opportunities from DHO_Application")
    public ResponseEntity<ApiResponse<List<DhoOpportunitiesDTO>>> findAll() {
        List<DhoOpportunitiesDTO> opportunities = service.findAllDTOs();
        ApiResponse<List<DhoOpportunitiesDTO>> response = ApiResponse.<List<DhoOpportunitiesDTO>>builder()
                .success(true)
                .message("Opportunities successfully retrieved.")
                .data(opportunities)
                .build();
        return ResponseEntity.ok(response);
    }
}
