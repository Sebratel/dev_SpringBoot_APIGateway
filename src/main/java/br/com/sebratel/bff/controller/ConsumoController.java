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
public class ConsumoController {

    private final ConsumoService service;

    public ConsumoController(ConsumoService service) {
        this.service = service;
    }

    @Operation(
            summary = "Listar clientes com alto consumo (>1TB)",
            description = "Retorna o consumo proporcional ao mês corrente. " +
                    "Sessões que iniciaram no mês anterior são fatiadas matematicamente " +
                    "para refletir apenas o tráfego gerado no período atual."
    )
    @ApiResponse(responseCode = "200", description = "Lista consolidada com sucesso")
    @GetMapping("/consumo-alto")
    public ResponseEntity<List<ConsumoDTO>> getConsumoAlto() {
        return ResponseEntity.ok(service.listarConsumoAlto());
    }

    @GetMapping("/consumo-alto-paginado")
    public ResponseEntity<Page<ConsumoDTO>> getConsumoAltoListado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.listarConsumoAltoPaginado(page, size));
    }

}