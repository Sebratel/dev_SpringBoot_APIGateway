package br.com.sebratel.bff.cucumber.controller;

import br.com.sebratel.bff.cucumber.CommonStepDefinitions;

import br.com.sebratel.bff.service.AuthenticationSitesService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class AuthenticationSitesStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthenticationSitesService authenticationSitesService;

    @Given("the authentication sites service is ready")
    public void the_authentication_sites_service_is_ready() {
        when(authenticationSitesService.execute(anyString())).thenReturn(List.of());
    }

    @When("I request to recover authentication sites")
    public void i_request_to_recover_authentication_sites() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/api/v1/sites").param("title", "test"));
        CommonStepDefinitions.setResultActions(resultActions);
    }
}
