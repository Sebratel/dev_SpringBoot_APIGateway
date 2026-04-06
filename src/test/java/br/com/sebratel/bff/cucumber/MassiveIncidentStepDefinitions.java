package br.com.sebratel.bff.cucumber;

import br.com.sebratel.bff.controller.MassiveElevenController;
import br.com.sebratel.bff.dto.massivas.CriacaoDeMassivaOutputDTO;
import br.com.sebratel.bff.service.massivas.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@CucumberContextConfiguration
@WebMvcTest(MassiveElevenController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
public class MassiveIncidentStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdicionarMassivaNoEllevenService adicionarMassivaNoEllevenService;

    @MockitoBean
    private AdicionarMassivaNoEllevenApiService adicionarMassivaNoEllevenApiService;

    @MockitoBean
    private EnviarListaDeAfetadosParaNativeService enviarListaDeAfetadosParaNativeService;

    @MockitoBean
    private GetAllMassivesService getAllMassivesService;

    @MockitoBean
    private RecuperarTodasAsMassivasPeloBancoService recuperarTodasAsMassivasPeloBancoService;

    @MockitoBean
    private FinalizarMassivaNoEllevenApiService finalizarMassivaNoEllevenApiService;

    private ResultActions resultActions;

    @Given("the ERP system is ready to accept a massive incident")
    public void the_erp_system_is_ready_to_accept_a_massive_incident() {
        CriacaoDeMassivaOutputDTO output = CriacaoDeMassivaOutputDTO.builder()
                .id("12345")
                .build();
        when(adicionarMassivaNoEllevenService.salvarNoBancoERP(any())).thenReturn(output);
    }

    @When("I submit a request to create a massive incident with:")
    public void i_submit_a_request_to_create_a_massive_incident_with(Map<String, String> data) throws Exception {
        // Prepare JSON based on data table
        // For simplicity, we create a JSON string directly or use a Map
        String json = objectMapper.writeValueAsString(data);
        
        // Correcting the accessPointIds if needed as it should be a list in the DTO
        // But for this demo, we'll just send the map which Jackson will handle
        
        resultActions = mockMvc.perform(post("/api/v1/massive-incidents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    @Then("the massive incident should be successfully created")
    public void the_massive_incident_should_be_successfully_created() throws Exception {
        resultActions.andExpect(status().isCreated())
                     .andExpect(jsonPath("$.success").value(true));
    }

    @And("the response should contain a valid incident ID")
    public void the_response_should_contain_a_valid_incident_id() throws Exception {
        resultActions.andExpect(jsonPath("$.data.id").value("12345"));
    }
}
