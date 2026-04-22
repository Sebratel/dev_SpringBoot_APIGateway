//package br.com.sebratel.bff.controller.dho;
//
//import br.com.sebratel.bff.dho.adapter.in.web.OpportunityController;
//import br.com.sebratel.bff.dho.adapter.in.web.PersonController;
//import br.com.sebratel.bff.dho.domain.model.DhoOpportunities;
//import br.com.sebratel.bff.dho.domain.model.DhoPeople;
//import br.com.sebratel.bff.dho.domain.port.in.OpportunityUseCase;
//import br.com.sebratel.bff.dho.domain.port.in.PersonUseCase;
//import br.com.sebratel.bff.dho.adapter.mapper.OpportunityMapper;
//import br.com.sebratel.bff.dho.adapter.mapper.PersonMapper;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.List;
//
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(controllers = {
//        OpportunityController.class,
//        PersonController.class
//})
//@Import({
//        OpportunityMapper.class,
//        PersonMapper.class
//})
//@AutoConfigureMockMvc(addFilters = false)
//@ActiveProfiles("test")
//class DhoIntegrationTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockitoBean
//    private OpportunityUseCase opportunityUseCase;
//
//    @MockitoBean
//    private PersonUseCase personUseCase;
//
//    @Test
//    @DisplayName("GET /api/dho/opportunities - Should return list of opportunities")
//    void findAll_DhoOpportunities_Success() throws Exception {
//        DhoOpportunities opportunity = new DhoOpportunities();
//        opportunity.setId(1);
//        opportunity.setObservations("V2 Opportunity");
//
//        when(opportunityUseCase.getAll()).thenReturn(List.of(opportunity));
//
//        mockMvc.perform(get("/api/dho/opportunities"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.data").isArray())
//                .andExpect(jsonPath("$.data[0].observations").value("V2 Opportunity"));
//    }
//
//    @Test
//    @DisplayName("GET /api/dho/people - Should return list of people")
//    void findAll_DhoPeople_Success() throws Exception {
//        DhoPeople person = new DhoPeople();
//        person.setId(1);
//        person.setName("John Doe");
//
//        when(personUseCase.getAll()).thenReturn(List.of(person));
//
//        mockMvc.perform(get("/api/dho/people"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.data").isArray())
//                .andExpect(jsonPath("$.data[0].name").value("John Doe"));
//    }
//}
