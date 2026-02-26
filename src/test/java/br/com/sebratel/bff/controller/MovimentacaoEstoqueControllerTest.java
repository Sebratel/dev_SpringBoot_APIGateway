package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.MovimentacaoEstoqueDTO;
import br.com.sebratel.bff.service.MovimentacaoEstoqueService;
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

@WebMvcTest(MovimentacaoEstoqueController.class)
@AutoConfigureMockMvc(addFilters = false)
class MovimentacaoEstoqueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MovimentacaoEstoqueService movimentacaoEstoqueService;

    @Test
    @DisplayName("Deve retornar 200 ao listar movimentações de estoque")
    void getEstoque_Sucesso() throws Exception {
        MovimentacaoEstoqueDTO dto = new MovimentacaoEstoqueDTO("", 1L, "", "",2.0, 1);
        List<MovimentacaoEstoqueDTO> lista = List.of(dto);

        when(movimentacaoEstoqueService.listarEstoque()).thenReturn(lista);

        mockMvc.perform(get("/api/v1/estoque/movimentacao")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }
}