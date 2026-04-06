package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.ContractActivationDTO;
import br.com.sebratel.bff.service.ContractActivationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/contract-activations", "/api/v1/contratos"})
public class ContractActivationController {

    @Autowired
    private ContractActivationService service;

    @GetMapping("/report")
    public ResponseEntity<List<ContractActivationDTO>> getReport() {
        return ResponseEntity.ok(service.getActivationReport());
    }
}