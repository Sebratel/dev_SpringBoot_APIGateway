package br.com.sebratel.bff.cucumber.controller.scripts;

import br.com.sebratel.bff.cucumber.CommonStepDefinitions;

import br.com.sebratel.bff.service.UltimaConexaoService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class LastConnectionStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UltimaConexaoService ultimaConexaoService;

    @Given("the last connection service is ready")
    public void the_last_connection_service_is_ready() {
        when(ultimaConexaoService.listarUltimasConexoes()).thenReturn(List.of());
    }

    @When("I request to recover last connections")
    public void i_request_to_recover_last_connections() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/api/v1/contracts/last-connections"));
        CommonStepDefinitions.setResultActions(resultActions);
    }
}
