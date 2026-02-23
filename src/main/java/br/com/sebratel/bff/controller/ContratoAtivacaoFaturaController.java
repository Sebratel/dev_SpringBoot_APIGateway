package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.ContratoAtivacaoFaturaDTO;
import br.com.sebratel.bff.service.ContratoAtivacaoFaturaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/contratos")
public class ContratoAtivacaoFaturaController {

    private final ContratoAtivacaoFaturaService service;

    public ContratoAtivacaoFaturaController(ContratoAtivacaoFaturaService service) {
        this.service = service;
    }

    @GetMapping("/ativacao-pendente-fatura")
    public ResponseEntity<List<ContratoAtivacaoFaturaDTO>> getContratos() {
        return ResponseEntity.ok(service.listarContratosRelacionados());
    }
}