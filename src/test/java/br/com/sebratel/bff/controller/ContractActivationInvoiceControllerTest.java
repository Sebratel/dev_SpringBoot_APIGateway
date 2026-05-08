package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.BaseTest;
import br.com.sebratel.bff.controller.scripts.ContractActivationInvoiceController;
import br.com.sebratel.bff.dto.ContratoAtivacaoFaturaDTO;
import br.com.sebratel.bff.service.ContratoAtivacaoFaturaService;
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

@WebMvcTest(ContractActivationInvoiceController.class)
@AutoConfigureMockMvc(addFilters = false)
class ContractActivationInvoiceControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContratoAtivacaoFaturaService contratoAtivacaoFaturaService;

    @Test
    @DisplayName("Should return 200 when listing contracts with pending activation invoice")
    void getContracts_Success() throws Exception {
        ContratoAtivacaoFaturaDTO dto = new ContratoAtivacaoFaturaDTO("", "", "", LocalDate.now().atStartOfDay(), LocalDate.now());
        List<ContratoAtivacaoFaturaDTO> list = List.of(dto);

        when(contratoAtivacaoFaturaService.listarContratosRelacionados()).thenReturn(list);

        mockMvc.perform(get("/api/v1/contracts/pending-activation-invoice")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
