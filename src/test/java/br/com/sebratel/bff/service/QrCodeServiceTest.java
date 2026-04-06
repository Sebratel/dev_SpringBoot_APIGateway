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
    private QrCodeService service;

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
}
