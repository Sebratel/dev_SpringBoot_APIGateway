package br.com.sebratel.bff.cucumber.controller;

import br.com.sebratel.bff.cucumber.CommonStepDefinitions;

import br.com.sebratel.bff.service.FirstAuthenticationService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class FirstAuthenticationStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FirstAuthenticationService firstAuthenticationService;

    @Given("the first authentication service is ready")
    public void the_first_authentication_service_is_ready() {
        when(firstAuthenticationService.listarPrimeirasAutenticacoes()).thenReturn(List.of());
    }

    @When("I request to recover first authentications")
    public void i_request_to_recover_first_authentications() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/api/v1/radius/first-authentications"));
        CommonStepDefinitions.setResultActions(resultActions);
    }
}
