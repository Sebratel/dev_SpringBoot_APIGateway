package br.com.sebratel.bff.cucumber.controller.scripts;

import br.com.sebratel.bff.cucumber.CommonStepDefinitions;

import br.com.sebratel.bff.dto.VendedoresAtivosDTO;
import br.com.sebratel.bff.service.VendedoresAtivosService;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class ActiveSellersStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VendedoresAtivosService vendedoresAtivosService;

    @Given("the active sellers service is ready")
    public void the_active_sellers_service_is_ready() {
        when(vendedoresAtivosService.listarVendedoresAtivos()).thenReturn(List.of(new VendedoresAtivosDTO("S", "e")));
    }

    @When("I request to recover active sellers")
    public void i_request_to_recover_active_sellers() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/api/v1/sellers/vendedores-ativos"));
        CommonStepDefinitions.setResultActions(resultActions);
    }

    @And("the response should contain a list of active sellers")
    public void the_response_should_contain_a_list_of_active_sellers() throws Exception {
        CommonStepDefinitions.getResultActions().andExpect(jsonPath("$").isArray());
    }
}
