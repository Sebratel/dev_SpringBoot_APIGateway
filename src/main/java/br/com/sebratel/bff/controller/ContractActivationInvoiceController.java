package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.ContratoAtivacaoFaturaDTO;
import br.com.sebratel.bff.service.ContratoAtivacaoFaturaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/contracts", "/api/v1/contrato-ativacao-fatura", "/api/v1/contratos"})
public class ContractActivationInvoiceController {

    private final ContratoAtivacaoFaturaService service;

    public ContractActivationInvoiceController(ContratoAtivacaoFaturaService service) {
        this.service = service;
    }

    @GetMapping({"/pending-activation-invoice", "/ativacao-pendente-fatura"})
    public ResponseEntity<List<ContratoAtivacaoFaturaDTO>> getContracts() {
        return ResponseEntity.ok(service.listarContratosRelacionados());
    }
}
