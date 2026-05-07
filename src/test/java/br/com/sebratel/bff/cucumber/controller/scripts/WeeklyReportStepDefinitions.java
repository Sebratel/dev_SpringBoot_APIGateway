package br.com.sebratel.bff.cucumber.controller.scripts;

import br.com.sebratel.bff.cucumber.CommonStepDefinitions;

import br.com.sebratel.bff.dto.ActiveSellersInputDTO;
import br.com.sebratel.bff.service.WeeklyReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class WeeklyReportStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WeeklyReportService weeklyReportService;

    @Given("the weekly report service is ready")
    public void the_weekly_report_service_is_ready() {
        when(weeklyReportService.sellersReportStream(anyString())).thenReturn(Stream.empty());
    }

    @When("I request to recover weekly report")
    public void i_request_to_recover_weekly_report() throws Exception {
        ActiveSellersInputDTO request = new ActiveSellersInputDTO();
        request.setNome("Test");
        ResultActions resultActions = mockMvc.perform(get("/api/v1/weekly-reports/vendedor")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
        CommonStepDefinitions.setResultActions(resultActions);
    }
}
