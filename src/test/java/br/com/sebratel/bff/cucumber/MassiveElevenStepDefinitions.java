package br.com.sebratel.bff.cucumber;

import br.com.sebratel.bff.dto.massivas.CriacaoDeMassivaOutputDTO;
import br.com.sebratel.bff.service.massivas.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MassiveElevenStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdicionarMassivaNoEllevenService adicionarMassivaNoEllevenService;

    @Given("the ERP system is ready to accept a massive incident")
    public void the_erp_system_is_ready_to_accept_a_massive_incident() {
        CriacaoDeMassivaOutputDTO output = CriacaoDeMassivaOutputDTO.builder()
                .protocolo("ERP-123")
                .id("1")
                .build();
        when(adicionarMassivaNoEllevenService.salvarNoBancoERP(any())).thenReturn(output);
    }

    @When("I submit a request to create a massive incident for splitters {string}")
    public void i_submit_a_request_to_create_a_massive_incident_for_splitters(String splitterIds) throws Exception {
        Map<String, Object> body = Map.of(
                "splitters", splitterIds.split(","),
                "tipoDeMassiva", "FIBRA_ROMPIDA",
                "previsaoDeNormalizacao", System.currentTimeMillis() + 3600000
        );

        ResultActions resultActions = mockMvc.perform(post("/api/v1/massivas/criar-massiva-no-elleven")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
        CommonStepDefinitions.setResultActions(resultActions);
    }

    @When("I submit a request to create a massive incident with:")
    public void i_submit_a_request_to_create_a_massive_incident_with(Map<String, String> data) throws Exception {
        Map<String, Object> body = new java.util.HashMap<>(data);
        body.put("accessPointIds", new Integer[]{1});
        body.put("cookieString", "test-cookie");

        ResultActions resultActions = mockMvc.perform(post("/api/v1/massivas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
        CommonStepDefinitions.setResultActions(resultActions);
    }

    @Then("the response should contain a valid incident ID")
    public void the_response_should_contain_a_valid_incident_id() throws Exception {
        CommonStepDefinitions.getResultActions().andExpect(jsonPath("$.data.protocolo").exists());
    }

    @Then("the massive incident should be successfully created")
    public void the_massive_incident_should_be_successfully_created() throws Exception {
        CommonStepDefinitions.getResultActions().andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.protocolo").value("ERP-123"));
    }
}
