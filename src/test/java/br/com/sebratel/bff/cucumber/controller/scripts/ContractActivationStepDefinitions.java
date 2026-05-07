package br.com.sebratel.bff.cucumber.controller.scripts;

import br.com.sebratel.bff.cucumber.CommonStepDefinitions;

import br.com.sebratel.bff.service.ContractActivationService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class ContractActivationStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ContractActivationService contractActivationService;

    @Given("the contract activation service is ready")
    public void the_contract_activation_service_is_ready() {
        when(contractActivationService.getActivationReport()).thenReturn(List.of());
    }

    @When("I request to recover contract activations")
    public void i_request_to_recover_contract_activations() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/api/v1/contract-activations/report"));
        CommonStepDefinitions.setResultActions(resultActions);
    }
}
