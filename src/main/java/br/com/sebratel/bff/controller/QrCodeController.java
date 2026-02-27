package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.QrCodeOutputDTO;
import br.com.sebratel.bff.service.QrCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/qr-code")
public class QrCodeController {


    private final QrCodeService qrCodeService;

    @Autowired
    public QrCodeController(QrCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    @GetMapping("/gerar")
    public ResponseEntity<QrCodeOutputDTO> executar(String json) throws Exception {
        QrCodeOutputDTO qrCodeOutputDTO = qrCodeService.gerarQrCodeParaFuncionario(json);
        return ResponseEntity.ok(qrCodeOutputDTO);
    }
}
