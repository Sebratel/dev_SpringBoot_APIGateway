package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.CriacaoDeMassivaInputDTO;
import br.com.sebratel.bff.dto.CriacaoDeMassivaOutputDTO;
import br.com.sebratel.bff.service.MassivasElevenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/massivas")
public class MassivasElevenController {

    private final MassivasElevenService massivasElevenService;

    @Autowired
    public MassivasElevenController(MassivasElevenService massivasElevenService) {
        this.massivasElevenService = massivasElevenService;
    }

    @PostMapping
    public ResponseEntity<CriacaoDeMassivaOutputDTO> criarMassivaComDadosDoFlutter(@RequestBody CriacaoDeMassivaInputDTO input) {
        CriacaoDeMassivaOutputDTO output = massivasElevenService.salvarNoBancoERP(input);
        return ResponseEntity.ok(output);
    }
}
