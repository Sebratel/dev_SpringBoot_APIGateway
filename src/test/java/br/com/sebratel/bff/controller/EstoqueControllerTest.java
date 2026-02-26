package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.EstoqueRequestDTO;
import br.com.sebratel.bff.dto.EstoqueTecnicoDTO;
import br.com.sebratel.bff.service.EstoqueService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EstoqueController.class)
@AutoConfigureMockMvc(addFilters = false)
class EstoqueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EstoqueService estoqueService;

    @Test
    @DisplayName("Deve retornar 200 ao buscar estoque por técnico")
    void getEstoque_Sucesso() throws Exception {
        EstoqueRequestDTO requestDTO = new EstoqueRequestDTO();
        requestDTO.setNome("Tecnico Teste");

        EstoqueTecnicoDTO responseDTO = new EstoqueTecnicoDTO("","","",1L, 1L);
        List<EstoqueTecnicoDTO> lista = List.of(responseDTO);

        when(estoqueService.buscarEstoquePorTecnico(anyString())).thenReturn(lista);

        mockMvc.perform(post("/api/v1/estoque/tecnico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("Deve retornar 400 quando o corpo do post estiver vazio")
    void getEstoque_BadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/estoque/tecnico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }
}