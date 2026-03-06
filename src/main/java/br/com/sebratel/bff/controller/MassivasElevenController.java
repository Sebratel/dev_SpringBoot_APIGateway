package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.ApiResponse;
import br.com.sebratel.bff.dto.CriacaoDeMassivaInputDTO;
import br.com.sebratel.bff.dto.CriacaoDeMassivaOutputDTO;
import br.com.sebratel.bff.dto.ListaDeAfetadosDTO;
import br.com.sebratel.bff.service.MassivasElevenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<ApiResponse<CriacaoDeMassivaOutputDTO>> criarMassivaComDadosDoFlutter(
            @Valid @RequestBody CriacaoDeMassivaInputDTO input) {

        CriacaoDeMassivaOutputDTO output = massivasElevenService.salvarNoBancoERP(input);

        ApiResponse<CriacaoDeMassivaOutputDTO> response = ApiResponse.<CriacaoDeMassivaOutputDTO>builder()
                .success(true)
                .message("Massiva criada com sucesso no ERP.")
                .data(output)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/enviar-dados-para-native")
    public ResponseEntity<ApiResponse<ListaDeAfetadosDTO>> enviarListaDosAfetadosParaNative(
            @Valid @RequestBody ListaDeAfetadosDTO input) {

        ListaDeAfetadosDTO output = massivasElevenService.enviarListaDosAfetadosParaNative(input);

        ApiResponse<ListaDeAfetadosDTO> response = ApiResponse.<ListaDeAfetadosDTO>builder()
                .success(true)
                .message("Usuarios listados enviados para native.")
                .data(output)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
