package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.RelatorioClienteNomeDuplicadoDTO;
import br.com.sebratel.bff.service.RelatorioClienteNomeDuplicadoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/relatorios")
public class RelatorioClienteNomeDuplicadoController {

    private final RelatorioClienteNomeDuplicadoService service;

    public RelatorioClienteNomeDuplicadoController(RelatorioClienteNomeDuplicadoService service) {
        this.service = service;
    }

    @GetMapping("/clientes-nomes-duplicados")
    public ResponseEntity<List<RelatorioClienteNomeDuplicadoDTO>> getClientesNomesDuplicados() {
        return ResponseEntity.ok(service.listarClientesNomesDuplicados());
    }
}