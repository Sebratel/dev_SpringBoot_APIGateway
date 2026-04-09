package br.com.sebratel.bff.cucumber.controller;

import br.com.sebratel.bff.cucumber.CommonStepDefinitions;

import br.com.sebratel.bff.dto.massivas.ImpactDetailsOutputDTO;
import br.com.sebratel.bff.dto.massivas.ImpactedUsersOutputDTO;
import br.com.sebratel.bff.service.AffectedUserService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class AffectedUserStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AffectedUserService affectedUSerService;

    @Given("the affected user service is ready with users")
    public void the_affected_user_service_is_ready_with_users() {
        ImpactDetailsOutputDTO details = ImpactDetailsOutputDTO.builder().reason("R").estimateTimeOfRestoration(1L).build();
        ImpactedUsersOutputDTO output = ImpactedUsersOutputDTO.builder().impactedUsers(List.of(Map.of(1L, details))).build();
        when(affectedUSerService.getAll()).thenReturn(output);
    }

    @Given("the affected user service has no users")
    public void the_affected_user_service_has_no_users() {
        when(affectedUSerService.getAll()).thenReturn(ImpactedUsersOutputDTO.builder().impactedUsers(List.of()).build());
    }

    @When("I request to get all impacted users")
    public void i_request_to_get_all_impacted_users() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/api/v1/impacted-users"));
        CommonStepDefinitions.setResultActions(resultActions);
    }

    @Then("the response success flag should be true")
    public void the_response_success_flag_should_be_true() throws Exception {
        CommonStepDefinitions.getResultActions().andExpect(jsonPath("$.success").value(true));
    }

    @Then("the response success flag should be false")
    public void the_response_success_flag_should_be_false() throws Exception {
        CommonStepDefinitions.getResultActions().andExpect(jsonPath("$.success").value(false));
    }
}
