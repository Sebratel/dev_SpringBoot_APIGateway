package br.com.sebratel.bff.cucumber.controller;

import br.com.sebratel.bff.cucumber.CommonStepDefinitions;

import br.com.sebratel.bff.dto.AquisicaoDTO;
import br.com.sebratel.bff.service.AquisicaoService;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class AcquisitionStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AquisicaoService aquisicaoService;

    @Given("the acquisition service is ready")
    public void the_acquisition_service_is_ready() {
        AquisicaoDTO dto = new AquisicaoDTO(1L, "C", "P", LocalDate.now(), "U", LocalDate.now(), 1.0, "B", "S", "O");
        when(aquisicaoService.listarAquisicoes()).thenReturn(List.of(dto));
    }

    @When("I request to recover acquisition orders")
    public void i_request_to_recover_acquisition_orders() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/api/v1/acquisitions/recover-acquisition-orders"));
        CommonStepDefinitions.setResultActions(resultActions);
    }

    @And("the response should contain a list of acquisitions")
    public void the_response_should_contain_a_list_of_acquisitions() throws Exception {
        CommonStepDefinitions.getResultActions().andExpect(jsonPath("$").isArray());
    }
}
