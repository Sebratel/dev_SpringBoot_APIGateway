package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.BaseTest;
import br.com.sebratel.bff.controller.scripts.FirstAuthenticationController;
import br.com.sebratel.bff.dto.FirstAuthenticationDTO;
import br.com.sebratel.bff.service.FirstAuthenticationService;
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

@WebMvcTest(FirstAuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
class FirstAuthenticationControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FirstAuthenticationService firstAuthenticationService;

    @Test
    @DisplayName("Deve retornar 200 ao listar as primeiras autenticações do radius")
    void getFirstAuthentications_Sucesso() throws Exception {
        FirstAuthenticationDTO dto = new FirstAuthenticationDTO("", LocalDate.now().atStartOfDay());
        List<FirstAuthenticationDTO> lista = List.of(dto);

        when(firstAuthenticationService.listarPrimeirasAutenticacoes()).thenReturn(lista);

        mockMvc.perform(get("/api/v1/radius/first-authentications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }
}