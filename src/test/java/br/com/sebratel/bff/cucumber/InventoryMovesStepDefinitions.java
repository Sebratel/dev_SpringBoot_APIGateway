package br.com.sebratel.bff.cucumber;

import br.com.sebratel.bff.service.InventoryMovesService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class InventoryMovesStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InventoryMovesService inventoryMovesService;

    @Given("the inventory moves service is ready")
    public void the_inventory_moves_service_is_ready() {
        when(inventoryMovesService.listarEstoque()).thenReturn(List.of());
    }

    @When("I request to recover inventory moves")
    public void i_request_to_recover_inventory_moves() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/api/v1/estoque/movimentacao"));
        CommonStepDefinitions.setResultActions(resultActions);
    }
}
