package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.ConsumoDTO;
import br.com.sebratel.bff.service.ConsumoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ConsumoController {

    private final ConsumoService service;

    public ConsumoController(ConsumoService service) {
        this.service = service;
    }

    @GetMapping("/consumo-alto")
    public ResponseEntity<List<ConsumoDTO>> getConsumoAlto() {
        return ResponseEntity.ok(service.listarConsumoAlto());
    }
}