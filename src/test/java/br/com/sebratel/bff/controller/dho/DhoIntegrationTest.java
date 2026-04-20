package br.com.sebratel.bff.controller.dho;

import br.com.sebratel.bff.controller.DhoOpportunityController;
import br.com.sebratel.bff.model.entity.DhoOpportunityEntity;
import br.com.sebratel.bff.model.entity.dho.DhoOpportunities;
import br.com.sebratel.bff.model.entity.dho.DhoPeople;
import br.com.sebratel.bff.repository.afetados.DhoOpportunityRepository;
import br.com.sebratel.bff.repository.afetados.dho.DhoOpportunitiesRepository;
import br.com.sebratel.bff.repository.afetados.dho.DhoPeopleRepository;
import br.com.sebratel.bff.service.DhoOpportunityService;
import br.com.sebratel.bff.service.dho.DhoOpportunitiesService;
import br.com.sebratel.bff.service.dho.DhoPeopleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {
        DhoOpportunityController.class,
        DhoOpportunitiesController.class,
        DhoPeopleController.class
})
@Import({
        DhoOpportunityService.class,
        DhoOpportunitiesService.class,
        DhoPeopleService.class
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class DhoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DhoOpportunityRepository dhoOpportunityRepository;

    @MockitoBean
    private DhoOpportunitiesRepository dhoOpportunitiesRepository;

    @MockitoBean
    private DhoPeopleRepository dhoPeopleRepository;

    @Test
    @DisplayName("GET /api/v1/dho-opportunities - Should return all opportunities")
    void findAll_DhoOpportunities_Success() throws Exception {
        DhoOpportunityEntity entity = new DhoOpportunityEntity();
        entity.setId(1);
        entity.setCargo("Developer");
        entity.setStatus("OPEN");

        when(dhoOpportunityRepository.findAll()).thenReturn(List.of(entity));

        mockMvc.perform(get("/api/v1/dho-opportunities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].cargo").value("Developer"));
    }

    @Test
    @DisplayName("GET /api/v1/dho-opportunities?status=OPEN - Should return filtered opportunities")
    void findByStatus_DhoOpportunities_Success() throws Exception {
        DhoOpportunityEntity entity = new DhoOpportunityEntity();
        entity.setId(1);
        entity.setCargo("Developer");
        entity.setStatus("OPEN");

        when(dhoOpportunityRepository.findAll()).thenReturn(List.of(entity));

        mockMvc.perform(get("/api/v1/dho-opportunities").param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].status").value("OPEN"));
    }

    @Test
    @DisplayName("GET /api/v1/dho-opportunities?status=CLOSED - Should return empty list when no match")
    void findByStatus_DhoOpportunities_Empty() throws Exception {
        DhoOpportunityEntity entity = new DhoOpportunityEntity();
        entity.setId(1);
        entity.setCargo("Developer");
        entity.setStatus("OPEN");

        when(dhoOpportunityRepository.findAll()).thenReturn(List.of(entity));

        mockMvc.perform(get("/api/v1/dho-opportunities").param("status", "CLOSED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("GET /api/v1/dho-opportunities - Should return 500 when repository fails")
    void findAll_DhoOpportunities_Failure() throws Exception {
        when(dhoOpportunityRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/v1/dho-opportunities"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error searching for opportunities: Database error"));
    }

    @Test
    @DisplayName("GET /api/v1/dho/opportunities - Should return list of opportunities V2")
    void findAll_DhoOpportunitiesV2_Success() throws Exception {
        DhoOpportunities opportunity = new DhoOpportunities();
        opportunity.setId(1);
        opportunity.setObservations("V2 Opportunity");

        when(dhoOpportunitiesRepository.findAll()).thenReturn(List.of(opportunity));

        mockMvc.perform(get("/api/v1/dho/opportunities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].observations").value("V2 Opportunity"));
    }

    @Test
    @DisplayName("GET /api/v1/dho/people - Should return list of people")
    void findAll_DhoPeople_Success() throws Exception {
        DhoPeople person = new DhoPeople();
        person.setId(1);
        person.setName("John Doe");

        when(dhoPeopleRepository.findAll()).thenReturn(List.of(person));

        mockMvc.perform(get("/api/v1/dho/people"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("John Doe"));
    }

    @Test
    @DisplayName("GET /api/v1/dho/opportunities - Should return empty list when no opportunities found")
    void findAll_DhoOpportunitiesV2_Empty() throws Exception {
        when(dhoOpportunitiesRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/dho/opportunities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("GET /api/v1/dho/people - Should return empty list when no people found")
    void findAll_DhoPeople_Empty() throws Exception {
        when(dhoPeopleRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/dho/people"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
