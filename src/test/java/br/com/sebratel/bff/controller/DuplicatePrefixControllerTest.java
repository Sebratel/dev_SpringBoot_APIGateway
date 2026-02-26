package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.DuplicatePrefixDTO;
import br.com.sebratel.bff.service.DuplicatePrefixService;
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

@WebMvcTest(DuplicatePrefixController.class)
@AutoConfigureMockMvc(addFilters = false)
class DuplicatePrefixControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DuplicatePrefixService duplicatePrefixService;

    @Test
    @DisplayName("Deve retornar 200 ao listar prefixos duplicados do radius")
    void getDuplicatePrefixes_Sucesso() throws Exception {
        DuplicatePrefixDTO dto = new DuplicatePrefixDTO("", 1L);
        List<DuplicatePrefixDTO> lista = List.of(dto);

        when(duplicatePrefixService.listarPrefixosDuplicados()).thenReturn(lista);

        mockMvc.perform(get("/api/v1/radius/duplicate-prefixes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }
}