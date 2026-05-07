package br.com.sebratel.bff.cucumber.controller.scripts;

import br.com.sebratel.bff.cucumber.CommonStepDefinitions;

import br.com.sebratel.bff.service.DuplicateCallingStationService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class DuplicateCallingStationStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DuplicateCallingStationService duplicateCallingStationService;

    @Given("the duplicate calling station service is ready")
    public void the_duplicate_calling_station_service_is_ready() {
        when(duplicateCallingStationService.listarConexoesDuplicadas()).thenReturn(List.of());
    }

    @When("I request to recover duplicate calling stations")
    public void i_request_to_recover_duplicate_calling_stations() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/api/v1/radius/duplicate-calling-stations"));
        CommonStepDefinitions.setResultActions(resultActions);
    }
}
