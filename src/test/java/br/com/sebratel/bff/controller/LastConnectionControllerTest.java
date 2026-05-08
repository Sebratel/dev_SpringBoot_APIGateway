package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.BaseTest;
import br.com.sebratel.bff.controller.scripts.LastConnectionController;
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

@WebMvcTest(LastConnectionController.class)
@AutoConfigureMockMvc(addFilters = false)
class LastConnectionControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UltimaConexaoService ultimaConexaoService;

    @Test
    @DisplayName("Should return 200 when listing last connections of contracts")
    void getLastConnections_Success() throws Exception {
        UltimaConexaoDTO dto = new UltimaConexaoDTO("", LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), 2L, 2L, "");
        List<UltimaConexaoDTO> list = List.of(dto);

        when(ultimaConexaoService.listarUltimasConexoes()).thenReturn(list);

        mockMvc.perform(get("/api/v1/contracts/last-connections")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
