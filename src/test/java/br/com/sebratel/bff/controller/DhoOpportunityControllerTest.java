package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.BaseTest;
import br.com.sebratel.bff.dto.DhoOpportunityDTO;
import br.com.sebratel.bff.service.DhoOpportunityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DhoOpportunityController.class)
@AutoConfigureMockMvc(addFilters = false)
class DhoOpportunityControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DhoOpportunityService service;

    @Test
    @DisplayName("Should return 200 and list of opportunities")
    void findAll_Success() throws Exception {
        DhoOpportunityDTO dto = new DhoOpportunityDTO(
                1, LocalDate.now(), "Developer", "New", "None",
                "Squad", "Tech", "Florianópolis", "OPEN", 15,
                LocalDate.now().plusDays(15), null, "On time",
                "Recruiter", null, null, null, "Manager", "Obs"
        );

        when(service.findAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/dho-opportunities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].cargo").value("Developer"));
    }

    @Test
    @DisplayName("Should return 200 and filtered list when status is provided")
    void findAll_WithStatus_Success() throws Exception {
        DhoOpportunityDTO dto = new DhoOpportunityDTO(
                1, LocalDate.now(), "Developer", "New", "None",
                "Squad", "Tech", "Florianópolis", "OPEN", 15,
                LocalDate.now().plusDays(15), null, "On time",
                "Recruiter", null, null, null, "Manager", "Obs"
        );

        when(service.findByStatus("OPEN")).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/dho-opportunities").param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].status").value("OPEN"));
    }

    @Test
    @DisplayName("Should return 500 when service fails")
    void findAll_Error() throws Exception {
        when(service.findAll()).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/v1/dho-opportunities"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error searching for opportunities: Database error"));
    }
}
