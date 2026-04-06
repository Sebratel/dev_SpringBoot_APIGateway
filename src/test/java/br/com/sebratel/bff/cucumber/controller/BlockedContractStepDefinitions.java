package br.com.sebratel.bff.cucumber.controller;

import br.com.sebratel.bff.cucumber.CommonStepDefinitions;

import br.com.sebratel.bff.service.ContratoBloqueadoService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class BlockedContractStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ContratoBloqueadoService contratoBloqueadoService;

    @Given("the blocked contract service is ready")
    public void the_blocked_contract_service_is_ready() {
        when(contratoBloqueadoService.listarContratosBloqueados()).thenReturn(List.of());
    }

    @When("I request to recover blocked contracts")
    public void i_request_to_recover_blocked_contracts() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/api/v1/contracts/blocked"));
        CommonStepDefinitions.setResultActions(resultActions);
    }
}
