package br.com.sebratel.bff.cucumber;

import br.com.sebratel.bff.service.ContratoAtivacaoFaturaService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class ContractActivationInvoiceStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ContratoAtivacaoFaturaService contratoAtivacaoFaturaService;

    @Given("the contract activation invoice service is ready")
    public void the_contract_activation_invoice_service_is_ready() {
        when(contratoAtivacaoFaturaService.listarContratosRelacionados()).thenReturn(List.of());
    }

    @When("I request to recover contract activation invoices")
    public void i_request_to_recover_contract_activation_invoice() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/api/v1/contracts/pending-activation-invoice"));
        CommonStepDefinitions.setResultActions(resultActions);
    }
}
