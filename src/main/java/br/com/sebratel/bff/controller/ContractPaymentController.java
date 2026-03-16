package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.ContractFirstPaymentDTO;
import br.com.sebratel.bff.service.ContractPaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/v1/contract-payments")
@Slf4j
public class ContractPaymentController {

    @Autowired
    private ContractPaymentService service;

    @GetMapping("/first-activation")
    public ResponseEntity<List<ContractFirstPaymentDTO>> getFirstPayments() {
        log.info("Iniciando busca para first-activations");
        return ResponseEntity.ok(service.getFirstPaymentReport());
    }
}