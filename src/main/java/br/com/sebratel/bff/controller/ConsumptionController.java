package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.ConsumoDTO;
import br.com.sebratel.bff.service.ConsumoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/v1")
public class ConsumptionController {

    private final ConsumoService service;

    public ConsumptionController(ConsumoService service) {
        this.service = service;
    }

    @Operation(
            summary = "List high consumption clients (>1TB)",
            description = "Returns the consumption proportional to the current month. " +
                    "Sessions that started in the previous month are mathematically sliced " +
                    "to reflect only the traffic generated in the current period."
    )
    @ApiResponse(responseCode = "200", description = "Consolidated list successfully retrieved")
    @GetMapping({"/high-consumption", "/consumo", "/consumo-alto"})
    public ResponseEntity<List<ConsumoDTO>> getHighConsumption() {
        return ResponseEntity.ok(service.listarConsumoAlto());
    }

    @GetMapping({"/high-consumption-paged", "/consumo-alto-paginado"})
    public ResponseEntity<Page<ConsumoDTO>> getHighConsumptionPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.listarConsumoAltoPaginado(page, size));
    }

}
