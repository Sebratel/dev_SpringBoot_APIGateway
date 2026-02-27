package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.QrCodeInputDTO;
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
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class QrCodeService {

    private static final Logger logger = LoggerFactory.getLogger(QrCodeService.class);

    public QrCodeOutputDTO gerarQrCodeParaFuncionario(QrCodeInputDTO jsonFuncionario) throws Exception {
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

        byte[] encryptedBytes = cipher.doFinal(jsonFuncionario.getJson().getBytes());
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

    public String decryptarQrCode(String encryptedData) throws Exception {
        try {
            ClassPathResource resource = new ClassPathResource("id_rsa_private.pem");

            if (!resource.exists()) {
                throw new RuntimeException("Chave privada não encontrada nos resources!");
            }

            String privateKeyPEM = new String(resource.getInputStream().readAllBytes())
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            // 2. Preparar a Chave Privada (Formato PKCS8)
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyPEM);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = kf.generatePrivate(spec);

            // 3. Configurar o Cipher para Desencriptar
            // IMPORTANTE: O algoritmo e o padding devem ser IDÊNTICOS aos usados na encriptação
            Cipher decryptCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);

            // 4. Decodificar o Base64 e Desencriptar
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedBytes = decryptCipher.doFinal(encryptedBytes);

            // 5. Retornar o JSON original como String
            return new String(decryptedBytes, "UTF-8");

        } catch (Exception e) {
            logger.error("Erro ao desencriptar dados do QR Code: {}", e.getMessage());
            throw new RuntimeException("Falha na autenticação dos dados: Código inválido ou chave incorreta.");
        }
    }
}