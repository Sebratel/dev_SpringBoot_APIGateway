package br.com.sebratel.bff.controller.scripts;

import br.com.sebratel.bff.dto.PatrimonioPendenteDTO;
import br.com.sebratel.bff.service.PatrimonioPendenteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/assets", "/api/v1/patrimonio-pendente", "/api/v1/patrimonios"})
public class PendingAssetController {

    private final PatrimonioPendenteService service;

    public PendingAssetController(PatrimonioPendenteService service) {
        this.service = service;
    }

    @GetMapping({"/pending", "/pendentes"})
    public ResponseEntity<List<PatrimonioPendenteDTO>> getPendingAssets() {
        return ResponseEntity.ok(service.listarPatrimoniosPendentes());
    }
}
