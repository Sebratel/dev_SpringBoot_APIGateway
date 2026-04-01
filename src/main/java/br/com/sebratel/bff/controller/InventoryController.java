package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.InventoryRequestDTO;
import br.com.sebratel.bff.dto.TechnicianInventoryDTO;
import br.com.sebratel.bff.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/estoque")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService service;

    @PostMapping("/tecnico")
    public ResponseEntity<List<TechnicianInventoryDTO>> getInventory(@RequestBody InventoryRequestDTO inventoryRequestDTO) {
        return ResponseEntity.ok(service.getInventoryByTechnician(inventoryRequestDTO.getNome()));
    }
}