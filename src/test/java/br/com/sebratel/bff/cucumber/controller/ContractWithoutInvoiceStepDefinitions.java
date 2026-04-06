package br.com.sebratel.bff.cucumber.controller;

import br.com.sebratel.bff.cucumber.CommonStepDefinitions;

import br.com.sebratel.bff.service.ContratoSemFaturaService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class ContractWithoutInvoiceStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ContratoSemFaturaService contratoSemFaturaService;

    @Given("the contract without invoice service is ready")
    public void the_contract_without_invoice_service_is_ready() {
        when(contratoSemFaturaService.listarContratosSemFatura()).thenReturn(List.of());
    }

    @When("I request to recover contracts without invoice")
    public void i_request_to_recover_contracts_without_invoice() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/api/v1/contracts/without-invoice"));
        CommonStepDefinitions.setResultActions(resultActions);
    }
}
