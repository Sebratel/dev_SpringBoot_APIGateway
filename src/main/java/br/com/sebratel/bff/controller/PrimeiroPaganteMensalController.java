package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.comercial.RelatorioPorVendedorDTO;
import br.com.sebratel.bff.service.comercial.PrimeiroPaganteMensalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/relatorios")
public class PrimeiroPaganteMensalController {


    private final PrimeiroPaganteMensalService primeiroPaganteMensalService;

    @Autowired
    private PrimeiroPaganteMensalController(PrimeiroPaganteMensalService primeiroPaganteMensalService) {
        this.primeiroPaganteMensalService = primeiroPaganteMensalService;
    }

    @GetMapping("/primeiro-pagante-mensal")
    public ResponseEntity<List<RelatorioPorVendedorDTO>> executar() {
        List<RelatorioPorVendedorDTO> response = primeiroPaganteMensalService.filtroELoop();
        return ResponseEntity.ok(response);
    }
}
