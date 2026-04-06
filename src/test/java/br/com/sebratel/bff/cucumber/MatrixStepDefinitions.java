package br.com.sebratel.bff.cucumber;

import br.com.sebratel.bff.dto.MatrixMassiveInputDTO;
import br.com.sebratel.bff.dto.MatrixMassiveOutputDTO;
import br.com.sebratel.bff.service.MatrixService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class MatrixStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MatrixService matrixService;

    @Given("the matrix service is ready")
    public void the_matrix_service_is_ready() {
        when(matrixService.getContractInfoByCPF(anyString())).thenReturn(new MatrixMassiveOutputDTO());
    }

    @When("I request to recover matrix data")
    public void i_request_to_recover_matrix_data() throws Exception {
        MatrixMassiveInputDTO inputDTO = new MatrixMassiveInputDTO();
        inputDTO.setCpf("12345678900");
        ResultActions resultActions = mockMvc.perform(get("/api/v1/matrix")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDTO)));
        CommonStepDefinitions.setResultActions(resultActions);
    }
}
