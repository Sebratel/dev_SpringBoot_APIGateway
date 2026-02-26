package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.RelatorioFinalDTO;
import br.com.sebratel.bff.dto.VendedoresAtivosInputDTO;
import br.com.sebratel.bff.service.ApoioSemanalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApoioSemanalController.class)
@AutoConfigureMockMvc(addFilters = false)
class ApoioSemanalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ApoioSemanalService apoioSemanalService;

    @Test
    @DisplayName("Deve retornar 200 ao buscar dados do vendedor")
    void getPorVendedor_Sucesso() throws Exception {
        VendedoresAtivosInputDTO input = new VendedoresAtivosInputDTO();
        input.setNome("nome");

        RelatorioFinalDTO dto = new RelatorioFinalDTO(
                null,
                null,
                "v3",
                "v4",
                "v5",
                "v6",
                "v7",
                "v8",
                "v9"
        );
        when(apoioSemanalService.streamRelatorioPorVendedor(anyString()))
                .thenReturn(Stream.of(dto));

        mockMvc.perform(get("/api/v1/contract-payments/vendedor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    @DisplayName("Deve retornar 400 quando o corpo da requisição é inválido")
    void getPorVendedor_BadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/contract-payments/vendedor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }
}