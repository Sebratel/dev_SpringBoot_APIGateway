package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.ContratoAtivacaoFaturaDTO;
import br.com.sebratel.bff.service.ContratoAtivacaoFaturaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContratoAtivacaoFaturaController.class)
@AutoConfigureMockMvc(addFilters = false)
class ContratoAtivacaoFaturaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContratoAtivacaoFaturaService contratoAtivacaoFaturaService;

    @Test
    @DisplayName("Deve retornar 200 ao listar contratos com ativação pendente de fatura")
    void getContratos_Sucesso() throws Exception {
        ContratoAtivacaoFaturaDTO dto = new ContratoAtivacaoFaturaDTO("", "", "", LocalDate.now().atStartOfDay(), LocalDate.now());
        List<ContratoAtivacaoFaturaDTO> lista = List.of(dto);

        when(contratoAtivacaoFaturaService.listarContratosRelacionados()).thenReturn(lista);

        mockMvc.perform(get("/api/v1/contratos/ativacao-pendente-fatura")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }
}