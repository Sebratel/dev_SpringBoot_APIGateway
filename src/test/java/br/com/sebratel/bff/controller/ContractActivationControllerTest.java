package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.BaseTest;
import br.com.sebratel.bff.controller.scripts.ContractActivationController;
import br.com.sebratel.bff.dto.ContractActivationDTO;
import br.com.sebratel.bff.service.ContractActivationService;
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

@WebMvcTest(ContractActivationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ContractActivationControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContractActivationService contractActivationService;

    @Test
    @DisplayName("Deve retornar 200 ao recuperar relatório de ativações")
    void getReport_Sucesso() throws Exception {
        ContractActivationDTO dto = new ContractActivationDTO();
        List<ContractActivationDTO> lista = List.of(dto);

        when(contractActivationService.getActivationReport()).thenReturn(lista);

        mockMvc.perform(get("/api/v1/contract-activations/report")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }
}