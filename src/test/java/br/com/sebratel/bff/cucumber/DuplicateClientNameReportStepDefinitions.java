package br.com.sebratel.bff.cucumber;

import br.com.sebratel.bff.service.RelatorioClienteNomeDuplicadoService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class DuplicateClientNameReportStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RelatorioClienteNomeDuplicadoService relatorioClienteNomeDuplicadoService;

    @Given("the duplicate client name report service is ready")
    public void the_duplicate_client_name_report_service_is_ready() {
        when(relatorioClienteNomeDuplicadoService.listarClientesNomesDuplicados()).thenReturn(List.of());
    }

    @When("I request to recover duplicate client name report")
    public void i_request_to_recover_duplicate_client_name_report() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/api/v1/reports/duplicate-client-names"));
        CommonStepDefinitions.setResultActions(resultActions);
    }
}
