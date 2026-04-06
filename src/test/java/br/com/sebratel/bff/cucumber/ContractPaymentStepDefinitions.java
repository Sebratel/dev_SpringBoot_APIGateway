package br.com.sebratel.bff.cucumber;

import br.com.sebratel.bff.service.ContractPaymentService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class ContractPaymentStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ContractPaymentService contractPaymentService;

    @Given("the contract payment service is ready")
    public void the_contract_payment_service_is_ready() {
        when(contractPaymentService.getFirstPaymentReport()).thenReturn(List.of());
    }

    @When("I request to recover contract payments")
    public void i_request_to_recover_contract_payments() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/api/v1/contract-payments/first-activation"));
        CommonStepDefinitions.setResultActions(resultActions);
    }
}
