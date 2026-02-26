package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.ContratoSemFaturaDTO;
import br.com.sebratel.bff.service.ContratoSemFaturaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContratoSemFaturaController.class)
@AutoConfigureMockMvc(addFilters = false)
class ContratoSemFaturaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContratoSemFaturaService contratoSemFaturaService;

    @Test
    @DisplayName("Deve retornar 200 ao listar contratos sem fatura")
    void getContratosSemFatura_Sucesso() throws Exception {
        ContratoSemFaturaDTO dto = new ContratoSemFaturaDTO("", "", 1L);
        List<ContratoSemFaturaDTO> lista = List.of(dto);

        when(contratoSemFaturaService.listarContratosSemFatura()).thenReturn(lista);

        mockMvc.perform(get("/api/v1/contratos/sem-fatura")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }
}