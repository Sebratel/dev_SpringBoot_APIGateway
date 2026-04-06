package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.RelatorioClienteNomeDuplicadoDTO;
import br.com.sebratel.bff.service.RelatorioClienteNomeDuplicadoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/reports", "/api/v1/relatorio-cliente-nome-duplicado", "/api/v1/relatorios"})
public class DuplicateClientNameReportController {

    private final RelatorioClienteNomeDuplicadoService service;

    public DuplicateClientNameReportController(RelatorioClienteNomeDuplicadoService service) {
        this.service = service;
    }

    @GetMapping({"/duplicate-client-names", "/clientes-nomes-duplicados"})
    public ResponseEntity<List<RelatorioClienteNomeDuplicadoDTO>> getDuplicateClientNames() {
        return ResponseEntity.ok(service.listarClientesNomesDuplicados());
    }
}
