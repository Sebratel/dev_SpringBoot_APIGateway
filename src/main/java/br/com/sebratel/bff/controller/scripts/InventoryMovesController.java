package br.com.sebratel.bff.controller.scripts;

import br.com.sebratel.bff.dto.InventoryMovesDTO;
import br.com.sebratel.bff.service.InventoryMovesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/estoque")
public class InventoryMovesController {

    private final InventoryMovesService inventoryMovesService;

    public InventoryMovesController(InventoryMovesService inventoryMovesService) {
        this.inventoryMovesService = inventoryMovesService;
    }

    @GetMapping("movimentacao")
    public ResponseEntity<List<InventoryMovesDTO>> getEstoque() {
        List<InventoryMovesDTO> lista = inventoryMovesService.listarEstoque();
        return ResponseEntity.ok(lista);
    }
}