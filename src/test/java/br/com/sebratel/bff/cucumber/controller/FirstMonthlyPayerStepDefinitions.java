package br.com.sebratel.bff.cucumber.controller;

import br.com.sebratel.bff.cucumber.CommonStepDefinitions;

import br.com.sebratel.bff.service.comercial.PrimeiroPaganteMensalService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class FirstMonthlyPayerStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PrimeiroPaganteMensalService primeiroPaganteMensalService;

    @Given("the first monthly payer service is ready")
    public void the_first_monthly_payer_service_is_ready() {
        when(primeiroPaganteMensalService.filtroELoop()).thenReturn(List.of());
    }

    @When("I request to recover first monthly payers")
    public void i_request_to_recover_first_monthly_payers() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/api/v1/reports/first-monthly-payer"));
        CommonStepDefinitions.setResultActions(resultActions);
    }
}
