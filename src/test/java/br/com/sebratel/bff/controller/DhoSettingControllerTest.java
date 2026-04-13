package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.BaseTest;
import br.com.sebratel.bff.dto.DhoSettingDTO;
import br.com.sebratel.bff.service.DhoSettingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DhoSettingController.class)
@AutoConfigureMockMvc(addFilters = false)
class DhoSettingControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DhoSettingService service;

    @Test
    @DisplayName("Should return 200 and list of settings")
    void findAll_Success() throws Exception {
        DhoSettingDTO dto = new DhoSettingDTO("Developer", "Squad", "New", "Tech", "Florianópolis", "OPEN", "Recruiter", "None", "None", "Active", "Superior", "Initial", "LinkedIn", "Manager");

        when(service.findAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/dho-settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].cargo").value("Developer"));
    }
}
