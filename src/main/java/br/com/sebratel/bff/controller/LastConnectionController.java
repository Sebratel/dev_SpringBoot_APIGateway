package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.UltimaConexaoDTO;
import br.com.sebratel.bff.service.UltimaConexaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/contracts", "/api/v1/ultima-conexao", "/api/v1/contratos"})
public class LastConnectionController {

    private final UltimaConexaoService service;

    public LastConnectionController(UltimaConexaoService service) {
        this.service = service;
    }

    @GetMapping({"/last-connections", "/ultimas-conexoes"})
    public ResponseEntity<List<UltimaConexaoDTO>> getLastConnections() {
        return ResponseEntity.ok(service.listarUltimasConexoes());
    }
}
