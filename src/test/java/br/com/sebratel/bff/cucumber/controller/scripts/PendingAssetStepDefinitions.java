package br.com.sebratel.bff.cucumber.controller.scripts;

import br.com.sebratel.bff.cucumber.CommonStepDefinitions;

import br.com.sebratel.bff.service.PatrimonioPendenteService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class PendingAssetStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PatrimonioPendenteService patrimonioPendenteService;

    @Given("the pending asset service is ready")
    public void the_pending_asset_service_is_ready() {
        when(patrimonioPendenteService.listarPatrimoniosPendentes()).thenReturn(List.of());
    }

    @When("I request to recover pending assets")
    public void i_request_to_recover_pending_assets() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/api/v1/assets/pending"));
        CommonStepDefinitions.setResultActions(resultActions);
    }
}
