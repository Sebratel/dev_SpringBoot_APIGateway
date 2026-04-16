package br.com.sebratel.bff.controller.dho;

import br.com.sebratel.bff.model.entity.dho.DhoPeople;
import br.com.sebratel.bff.service.dho.DhoPeopleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dho/people")
@RequiredArgsConstructor
@Tag(name = "DHO People", description = "Endpoints for DHO people management")
public class DhoPeopleController {
    private final DhoPeopleService service;

    @GetMapping
    @Operation(summary = "List all people from DHO_Application")
    public ResponseEntity<List<DhoPeople>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }
}
