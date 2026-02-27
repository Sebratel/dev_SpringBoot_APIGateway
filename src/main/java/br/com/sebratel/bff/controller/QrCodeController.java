package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.QrCodeInputDTO;
import br.com.sebratel.bff.dto.QrCodeOutputDTO;
import br.com.sebratel.bff.service.QrCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    public ResponseEntity<QrCodeOutputDTO> executar(@RequestBody QrCodeInputDTO qrCodeInputDTO) throws Exception {
        QrCodeOutputDTO qrCodeOutputDTO = qrCodeService.gerarQrCodeParaFuncionario(qrCodeInputDTO);
        return ResponseEntity.ok(qrCodeOutputDTO);
    }

    @GetMapping("/recuperar")
    public ResponseEntity<String> recuperarQrCode(@RequestBody QrCodeInputDTO qrCodeInputDTO) throws Exception {
        String qrCodeOutputDTO = qrCodeService.decryptarQrCode(qrCodeInputDTO.getJson());
        return ResponseEntity.ok(qrCodeOutputDTO);
    }

}
