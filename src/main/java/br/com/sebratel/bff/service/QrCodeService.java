package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.QrCodeOutputDTO;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class QrCodeService {

    private static final Logger logger = LoggerFactory.getLogger(QrCodeService.class);

    public QrCodeOutputDTO gerarQrCodeParaFuncionario(String jsonFuncionario) throws Exception {
        // 1. Localizar Chave Pública em ~/.ssh/id_rsa_public.pem

        ClassPathResource resource = new ClassPathResource("id_rsa_public.pem");

        if(!resource.exists()) {
          throw new RuntimeException("Chave publica nao encontrada");
    }






        String publicKeyPEM = new String(resource.getInputStream().readAllBytes())
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        // 2. Preparar a Chave para o Java
        byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);

        // 3. Encriptar o JSON (O conteúdo do QR Code será este resultado)
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] encryptedBytes = cipher.doFinal(jsonFuncionario.getBytes());
        String encryptedBase64 = Base64.getEncoder().encodeToString(encryptedBytes);

        logger.info("JSON encriptado com sucesso. Tamanho da string: {}", encryptedBase64.length());

        // 4. Gerar o QR Code contendo a STRING CRIPTOGRAFADA
        String qrCodeBase64 = criarImagemQRCode(encryptedBase64);

        // 5. Montar Retorno
        QrCodeOutputDTO dto = new QrCodeOutputDTO();
        dto.setEncryptedData(encryptedBase64); // Texto puro encriptado
        dto.setQrCodeImage(qrCodeBase64);      // Imagem em Base64 para o Front-end

        return dto;
    }

    private String criarImagemQRCode(String conteudoParaOQrCode) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        // Configurações para melhorar a leitura de dados densos (RSA)
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M); // Nível médio de correção
        hints.put(EncodeHintType.MARGIN, 1);

        BitMatrix bitMatrix = qrCodeWriter.encode(conteudoParaOQrCode, BarcodeFormat.QR_CODE, 500, 500, hints);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
            byte[] imageBytes = baos.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        }
    }
}
