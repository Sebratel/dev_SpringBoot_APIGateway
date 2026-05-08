package br.com.sebratel.bff.controller.scripts;

import br.com.sebratel.bff.dto.AquisicaoDTO;
import br.com.sebratel.bff.service.AquisicaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/acquisitions", "/api/v1/aquisicoes"})
public class AcquisitionController {

    private final AquisicaoService service;

    public AcquisitionController(AquisicaoService service) {
        this.service = service;
    }

    @GetMapping({"/recover-acquisition-orders", "/recuperar-pedidos-de-aquisicao"})
    public ResponseEntity<List<AquisicaoDTO>> getAcquisitions() {
        return ResponseEntity.ok(service.listarAquisicoes());
    }
}
