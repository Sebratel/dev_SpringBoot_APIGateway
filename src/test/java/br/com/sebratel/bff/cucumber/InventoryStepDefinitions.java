package br.com.sebratel.bff.cucumber;

import br.com.sebratel.bff.dto.InventoryRequestDTO;
import br.com.sebratel.bff.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class InventoryStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InventoryService inventoryService;

    @Given("the inventory service is ready")
    public void the_inventory_service_is_ready() {
        when(inventoryService.getInventoryByTechnician(anyString())).thenReturn(List.of());
    }

    @When("I request to recover inventory data")
    public void i_request_to_recover_inventory_data() throws Exception {
        InventoryRequestDTO request = new InventoryRequestDTO();
        request.setNome("Test");
        ResultActions resultActions = mockMvc.perform(post("/api/v1/estoque/tecnico")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
        CommonStepDefinitions.setResultActions(resultActions);
    }
}
