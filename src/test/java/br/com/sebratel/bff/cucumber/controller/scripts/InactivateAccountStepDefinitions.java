package br.com.sebratel.bff.cucumber.controller.scripts;

import br.com.sebratel.bff.cucumber.CommonStepDefinitions;
import br.com.sebratel.bff.dto.InactivateAccountDTO;
import br.com.sebratel.bff.service.InactivateAccountProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class InactivateAccountStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InactivateAccountProducer producer;

    @Given("the inactivate account producer is ready")
    public void the_inactivate_account_producer_is_ready() {
        // Mock is already injected by CucumberSpringConfiguration
    }

    @When("I request to inactivate an account with name {string} and cpf {string}")
    public void i_request_to_inactivate_an_account_with_name_and_cpf(String name, String cpf) throws Exception {
        InactivateAccountDTO.InactivateAccountUserInfo userInfo = InactivateAccountDTO.InactivateAccountUserInfo.builder()
                .name(name)
                .cpf(cpf)
                .build();

        InactivateAccountDTO request = InactivateAccountDTO.builder()
                .userInfo(userInfo)
                .build();

        ResultActions resultActions = mockMvc.perform(post("/api/v1/inactivate-account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        CommonStepDefinitions.setResultActions(resultActions);
    }
}
