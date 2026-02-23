package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.ContratoBloqueadoDTO;
import br.com.sebratel.bff.service.ContratoBloqueadoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/contratos")
public class ContratoBloqueadoController {

    private final ContratoBloqueadoService service;

    public ContratoBloqueadoController(ContratoBloqueadoService service) {
        this.service = service;
    }

    @GetMapping("/bloqueados")
    public ResponseEntity<List<ContratoBloqueadoDTO>> getContratosBloqueados() {
        return ResponseEntity.ok(service.listarContratosBloqueados());
    }
}