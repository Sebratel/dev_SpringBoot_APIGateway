package br.com.sebratel.bff.controller.dho;

import br.com.sebratel.bff.model.entity.dho.DhoOpportunities;
import br.com.sebratel.bff.service.dho.DhoOpportunitiesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dho/opportunities")
@RequiredArgsConstructor
@Tag(name = "DHO Opportunities V2", description = "New endpoints for DHO opportunities")
public class DhoOpportunitiesController {
    private final DhoOpportunitiesService service;

    @GetMapping
    @Operation(summary = "List all opportunities from DHO_Application")
    public ResponseEntity<List<DhoOpportunities>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }
}
