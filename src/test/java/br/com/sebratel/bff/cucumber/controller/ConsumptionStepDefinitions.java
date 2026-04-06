package br.com.sebratel.bff.cucumber.controller;

import br.com.sebratel.bff.cucumber.CommonStepDefinitions;

import br.com.sebratel.bff.dto.ConsumoDTO;
import br.com.sebratel.bff.service.ConsumoService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class ConsumptionStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConsumoService consumoService;

    @Given("the consumption service is ready")
    public void the_consumption_service_is_ready() {
        when(consumoService.listarConsumoAlto()).thenReturn(List.of(new ConsumoDTO("a", "b", "c", "d", 1.0, 1.0, 1.0)));
    }

    @When("I request to recover consumption data for contract {int}")
    public void i_request_to_recover_consumption_data_for_contract(int contractId) throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/api/v1/consumo"));
        CommonStepDefinitions.setResultActions(resultActions);
    }
}
