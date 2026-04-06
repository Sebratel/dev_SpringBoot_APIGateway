package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.ContratoSemFaturaDTO;
import br.com.sebratel.bff.service.ContratoSemFaturaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/contracts", "/api/v1/contrato-sem-fatura", "/api/v1/contratos"})
public class ContractWithoutInvoiceController {

    private final ContratoSemFaturaService service;

    public ContractWithoutInvoiceController(ContratoSemFaturaService service) {
        this.service = service;
    }

    @GetMapping({"/without-invoice", "/sem-fatura"})
    public ResponseEntity<List<ContratoSemFaturaDTO>> getContractsWithoutInvoice() {
        return ResponseEntity.ok(service.listarContratosSemFatura());
    }
}
