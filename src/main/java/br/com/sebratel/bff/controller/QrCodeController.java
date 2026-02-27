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
@RequestMapping("/api/v1/qr-code")
public class QrCodeController {

    @Autowired
    private QrCodeService qrCodeService;

    @GetMapping("/gerar")
    public ResponseEntity<List<RelatorioPorVendedorDTO>> executar(QrCodeInputDTO) {
        QrCodeOutputDTO QrCodeOutputDTO = qrCodeService.gerarQrCode();
        return ResponseEntity.ok(null);
    }
}
