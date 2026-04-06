package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.QrCodeInputDTO;
import br.com.sebratel.bff.dto.QrCodeOutputDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class QrCodeServiceTest {

    @InjectMocks
    private QrCodeService service = new QrCodeService();

    @Test
    void gerarEDecryptar_ShouldWorkTogether() throws Exception {
        // Arrange
        String originalJson = "{\"id\":1, \"name\":\"John Doe\"}";
        QrCodeInputDTO input = new QrCodeInputDTO();
        input.setJson(originalJson);

        // Act - Generate
        QrCodeOutputDTO output = service.gerarQrCodeParaFuncionario(input);

        // Assert - Generate
        assertNotNull(output);
        assertNotNull(output.getEncryptedData());
        assertTrue(output.getQrCodeImage().startsWith("data:image/png;base64,"));

        // Act - Decrypt
        String decryptedJson = service.decryptarQrCode(output.getEncryptedData());

        // Assert - Decrypt
        assertEquals(originalJson, decryptedJson);
    }

    @Test
    void decryptarQrCode_ShouldThrowException_WhenDataIsInvalid() {
        // Arrange
        String invalidData = "not-base64-and-not-encrypted";

        // Act & Assert
        assertThrows(RuntimeException.class, () -> service.decryptarQrCode(invalidData));
    }

    @Test
    void gerarQrCode_ShouldThrowException_WhenPublicKeyNotFound() {
        QrCodeService serviceWithMissingKey = new QrCodeService("non_existent.pem", "id_rsa_private.pem");
        QrCodeInputDTO input = new QrCodeInputDTO();
        input.setJson("{}");

        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> serviceWithMissingKey.gerarQrCodeParaFuncionario(input));
        assertEquals("Chave publica nao encontrada", exception.getMessage());
    }

    @Test
    void decryptarQrCode_ShouldThrowException_WhenPrivateKeyNotFound() {
        QrCodeService serviceWithMissingKey = new QrCodeService("id_rsa_public.pem", "non_existent.pem");

        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> serviceWithMissingKey.decryptarQrCode("someData"));
        assertEquals("Falha na autenticação dos dados: Código inválido ou chave incorreta.", exception.getMessage());
    }
}
