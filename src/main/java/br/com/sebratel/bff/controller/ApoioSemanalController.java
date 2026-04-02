package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.OrderedApoioSemanalDTO;
import br.com.sebratel.bff.dto.RelatorioFinalDTO;
import br.com.sebratel.bff.dto.VendedoresAtivosInputDTO;
import br.com.sebratel.bff.service.ApoioSemanalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/apoio-semanal")
@Slf4j
public class ApoioSemanalController {

// apoio semanadal services
    private final ApoioSemanalService apoioSemanalService;
    
    @Autowired
    public ApoioSemanalController(ApoioSemanalService apoioSemanalService) {
        this.apoioSemanalService = apoioSemanalService;
    }

    @GetMapping(value = "/vendedor")
    public ResponseEntity<OrderedApoioSemanalDTO> getPorVendedor(@RequestBody VendedoresAtivosInputDTO vendedoresAtivosInputDTO) {
        log.info("Iniciando relatorio para {}", vendedoresAtivosInputDTO.getNome());
        List<RelatorioFinalDTO> list = apoioSemanalService.streamRelatorioPorVendedor(vendedoresAtivosInputDTO.getNome()).toList();
        return ResponseEntity.ok(new OrderedApoioSemanalDTO(list));
    }
}