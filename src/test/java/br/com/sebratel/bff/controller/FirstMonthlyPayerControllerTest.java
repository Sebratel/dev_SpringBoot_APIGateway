package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.BaseTest;
import br.com.sebratel.bff.dto.comercial.RelatorioPorVendedorDTO;
import br.com.sebratel.bff.service.comercial.PrimeiroPaganteMensalService;
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

@WebMvcTest(FirstMonthlyPayerController.class)
@AutoConfigureMockMvc(addFilters = false)
class FirstMonthlyPayerControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PrimeiroPaganteMensalService primeiroPaganteMensalService;

    @Test
    @DisplayName("Should return 200 when executing first monthly payer report")
    void execute_Success() throws Exception {
        RelatorioPorVendedorDTO dto = new RelatorioPorVendedorDTO();
        List<RelatorioPorVendedorDTO> response = List.of(dto);

        when(primeiroPaganteMensalService.filtroELoop()).thenReturn(response);

        mockMvc.perform(get("/api/v1/reports/first-monthly-payer")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
