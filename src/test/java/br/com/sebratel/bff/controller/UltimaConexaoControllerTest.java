package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.UltimaConexaoDTO;
import br.com.sebratel.bff.service.UltimaConexaoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UltimaConexaoController.class)
@AutoConfigureMockMvc(addFilters = false)
class UltimaConexaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UltimaConexaoService ultimaConexaoService;

    @Test
    @DisplayName("Deve retornar 200 ao listar últimas conexões dos contratos")
    void getUltimasConexoes_Sucesso() throws Exception {
        UltimaConexaoDTO dto = new UltimaConexaoDTO("", LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), 2L, 2L, "");
        List<UltimaConexaoDTO> lista = List.of(dto);

        when(ultimaConexaoService.listarUltimasConexoes()).thenReturn(lista);

        mockMvc.perform(get("/api/v1/contratos/ultimas-conexoes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }
}