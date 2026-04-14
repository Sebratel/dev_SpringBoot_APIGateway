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

import java.time.LocalDateTime;
import java.util.List;

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
    @DisplayName("Should return 200 and list of collaborators")
    void findAll_Success() throws Exception {
        DhoOpportunityDTO dto = new DhoOpportunityDTO(
                1L, 123, "test@test.com", LocalDateTime.now(), "ACTIVE",
                "Florianópolis", "Tech", "Developer", "Supervisor",
                "Manager", "Coordinator"
        );

        when(service.findAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/dho-opportunities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].email").value("test@test.com"));
    }

    @Test
    @DisplayName("Should return 200 and filtered list when status is provided")
    void findAll_WithStatus_Success() throws Exception {
        DhoOpportunityDTO dto = new DhoOpportunityDTO(
                1L, 123, "test@test.com", LocalDateTime.now(), "ACTIVE",
                "Florianópolis", "Tech", "Developer", "Supervisor",
                "Manager", "Coordinator"
        );

        when(service.findByStatus("ACTIVE")).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/dho-opportunities").param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Should return 500 when service fails")
    void findAll_Error() throws Exception {
        when(service.findAll()).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/v1/dho-opportunities"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error searching for collaborators: Database error"));
    }
}
