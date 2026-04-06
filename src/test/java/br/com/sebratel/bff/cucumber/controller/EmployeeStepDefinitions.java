package br.com.sebratel.bff.cucumber.controller;

import br.com.sebratel.bff.cucumber.CommonStepDefinitions;

import br.com.sebratel.bff.service.EmployeeService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class EmployeeStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeService employeeService;

    @Given("the employee service is ready")
    public void the_employee_service_is_ready() {
        when(employeeService.getPersonIdByEmail(anyString())).thenReturn(1L);
    }

    @When("I request to recover employees")
    public void i_request_to_recover_employees() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/api/v1/employee/get-person-id-by-email").param("email", "test@example.com"));
        CommonStepDefinitions.setResultActions(resultActions);
    }
}
