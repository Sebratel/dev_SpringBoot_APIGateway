package br.com.sebratel.bff.controller.scripts;

import br.com.sebratel.bff.dto.DuplicatePrefixDTO;
import br.com.sebratel.bff.service.DuplicatePrefixService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/radius")
public class DuplicatePrefixController {

    private final DuplicatePrefixService service;

    public DuplicatePrefixController(DuplicatePrefixService service) {
        this.service = service;
    }

    @GetMapping("/duplicate-prefixes")
    public ResponseEntity<List<DuplicatePrefixDTO>> getDuplicatePrefixes() {
        return ResponseEntity.ok(service.listarPrefixosDuplicados());
    }
}