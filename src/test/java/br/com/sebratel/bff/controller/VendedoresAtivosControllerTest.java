package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.VendedoresAtivosDTO;
import br.com.sebratel.bff.service.VendedoresAtivosService;
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

@WebMvcTest(VendedoresAtivosController.class)
@AutoConfigureMockMvc(addFilters = false)
class VendedoresAtivosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VendedoresAtivosService vendedoresAtivosService;

    @Test
    @DisplayName("Deve retornar 200 ao listar vendedores ativos")
    void getAtivos_Sucesso() throws Exception {
        VendedoresAtivosDTO dto = new VendedoresAtivosDTO(
                "", ""
        );
        List<VendedoresAtivosDTO> lista = List.of(dto);

        when(vendedoresAtivosService.listarVendedoresAtivos()).thenReturn(lista);

        mockMvc.perform(get("/api/v1/vendedores/ativos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }
}