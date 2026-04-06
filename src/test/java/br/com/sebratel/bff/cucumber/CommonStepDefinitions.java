package br.com.sebratel.bff.cucumber;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CommonStepDefinitions {

    private static ResultActions lastResultActions;

    public static void setResultActions(ResultActions resultActions) {
        lastResultActions = resultActions;
    }

    public static ResultActions getResultActions() {
        return lastResultActions;
    }

    @Then("the response status should be {int}")
    public void the_response_status_should_be(int status) throws Exception {
        lastResultActions.andExpect(status().is(status));
    }

    @And("the response status should be {int} for common")
    public void the_response_status_should_be_common(int status) throws Exception {
        lastResultActions.andExpect(status().is(status));
    }
}
