package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.AquisicaoDTO;
import br.com.sebratel.bff.service.AquisicaoService;
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

@WebMvcTest(AquisicaoController.class)
@AutoConfigureMockMvc(addFilters = false)
class AquisicaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AquisicaoService aquisicaoService;

    @Test
    @DisplayName("Deve retornar 200 e a lista de aquisições com sucesso")
    void getAquisicoes_Sucesso() throws Exception {
        AquisicaoDTO dto = new AquisicaoDTO(1L, "", "", LocalDate.now(), "String", LocalDate.now(), 2.0, "String", "String");
        List<AquisicaoDTO> lista = List.of(dto);

        when(aquisicaoService.listarAquisicoes()).thenReturn(lista);

        mockMvc.perform(get("/api/v1/aquisicoes/recuperar-pedidos-de-aquisicao")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$").isArray());
    }
}