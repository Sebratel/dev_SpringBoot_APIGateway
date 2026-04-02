package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.BaseTest;
import br.com.sebratel.bff.dto.PatrimonioPendenteDTO;
import br.com.sebratel.bff.service.PatrimonioPendenteService;
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

@WebMvcTest(PatrimonioPendenteController.class)
@AutoConfigureMockMvc(addFilters = false)
class PatrimonioPendenteControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PatrimonioPendenteService patrimonioPendenteService;

    @Test
    @DisplayName("Deve retornar 200 ao listar patrimônios pendentes")
    void getPendentes_Sucesso() throws Exception {
        PatrimonioPendenteDTO dto = new PatrimonioPendenteDTO("", "", "", 2.0);
        List<PatrimonioPendenteDTO> lista = List.of(dto);

        when(patrimonioPendenteService.listarPatrimoniosPendentes()).thenReturn(lista);

        mockMvc.perform(get("/api/v1/patrimonios/pendentes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }
}