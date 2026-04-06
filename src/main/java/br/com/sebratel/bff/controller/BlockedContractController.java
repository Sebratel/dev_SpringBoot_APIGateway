package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.ContratoBloqueadoDTO;
import br.com.sebratel.bff.service.ContratoBloqueadoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/contracts", "/api/v1/contrato-bloqueado", "/api/v1/contratos"})
public class BlockedContractController {

    private final ContratoBloqueadoService service;

    public BlockedContractController(ContratoBloqueadoService service) {
        this.service = service;
    }

    @GetMapping({"/blocked", "/bloqueados"})
    public ResponseEntity<List<ContratoBloqueadoDTO>> getBlockedContracts() {
        return ResponseEntity.ok(service.listarContratosBloqueados());
    }
}
