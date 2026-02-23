package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.UltimaConexaoDTO;
import br.com.sebratel.bff.service.UltimaConexaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/contratos")
public class UltimaConexaoController {

    private final UltimaConexaoService service;

    public UltimaConexaoController(UltimaConexaoService service) {
        this.service = service;
    }

    @GetMapping("/ultimas-conexoes")
    public ResponseEntity<List<UltimaConexaoDTO>> getUltimasConexoes() {
        return ResponseEntity.ok(service.listarUltimasConexoes());
    }
}