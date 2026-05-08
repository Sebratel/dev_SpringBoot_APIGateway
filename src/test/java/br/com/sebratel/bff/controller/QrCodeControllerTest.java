package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.BaseTest;
import br.com.sebratel.bff.controller.scripts.QrCodeController;
import br.com.sebratel.bff.dto.QrCodeInputDTO;
import br.com.sebratel.bff.dto.QrCodeOutputDTO;
import br.com.sebratel.bff.service.QrCodeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QrCodeController.class)
@AutoConfigureMockMvc(addFilters = false)
class QrCodeControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private QrCodeService qrCodeService;

    @Test
    @DisplayName("Should generate QR code")
    void executar_Success() throws Exception {
        QrCodeInputDTO input = new QrCodeInputDTO();
        QrCodeOutputDTO output = new QrCodeOutputDTO();

        when(qrCodeService.gerarQrCodeParaFuncionario(any())).thenReturn(output);

        mockMvc.perform(get("/api/v1/qr-code/gerar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should recover QR code")
    void recuperarQrCode_Success() throws Exception {
        QrCodeInputDTO input = new QrCodeInputDTO();
        input.setJson("encrypted-data");

        when(qrCodeService.decryptarQrCode(anyString())).thenReturn("decrypted-data");

        mockMvc.perform(post("/api/v1/qr-code/recuperar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(content().string("decrypted-data"));
    }
}
