package br.com.sebratel.bff.controller.dho;

import br.com.sebratel.bff.BaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class DhoControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetPeople() throws Exception {
        mockMvc.perform(get("/api/v1/dho/people"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    public void testGetOpportunities() throws Exception {
        mockMvc.perform(get("/api/v1/dho/opportunities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    public void testGetSettings() throws Exception {
        mockMvc.perform(get("/api/v1/dho/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.teams").isArray())
                .andExpect(jsonPath("$.data.positions").isArray());
    }
}
