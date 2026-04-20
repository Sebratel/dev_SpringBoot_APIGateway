package br.com.sebratel.bff.controller.dho;

import br.com.sebratel.bff.BaseTest;
import br.com.sebratel.bff.service.dho.DhoOpportunitiesService;
import br.com.sebratel.bff.service.dho.DhoPeopleService;
import br.com.sebratel.bff.service.dho.DhoSettingsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.HashMap;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class DhoControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DhoPeopleService dhoPeopleService;

    @MockitoBean
    private DhoOpportunitiesService dhoOpportunitiesService;

    @MockitoBean
    private DhoSettingsService dhoSettingsService;

    @Test
    public void testGetPeople() throws Exception {
        when(dhoPeopleService.findAllDTOs()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/dho/people"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    public void testGetOpportunities() throws Exception {
        when(dhoOpportunitiesService.findAllDTOs()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/dho/opportunities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    public void testGetSettings() throws Exception {
        when(dhoSettingsService.getAllSettings()).thenReturn(new HashMap<>());

        mockMvc.perform(get("/api/v1/dho/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isMap());
    }
}
