package br.com.sebratel.bff.cucumber.controller;

import br.com.sebratel.bff.cucumber.CommonStepDefinitions;

import br.com.sebratel.bff.dto.splitters.EllevenSplitterResponseDTO;
import br.com.sebratel.bff.service.ListarSplittersService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class SplittersStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ListarSplittersService listarSplittersService;

    @Given("the splitters service is ready")
    public void the_splitters_service_is_ready() {
        when(listarSplittersService.executar()).thenReturn(new EllevenSplitterResponseDTO<>(true, null, List.of(), "type", 0L));
    }

    @When("I request to list all splitters")
    public void i_request_to_list_all_splitters() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/api/v1/splitters/listarSplitters"));
        CommonStepDefinitions.setResultActions(resultActions);
    }
}
