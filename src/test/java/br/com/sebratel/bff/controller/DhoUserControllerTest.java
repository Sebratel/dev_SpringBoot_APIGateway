package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.BaseTest;
import br.com.sebratel.bff.dto.DhoUserDTO;
import br.com.sebratel.bff.service.DhoUserService;
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

@WebMvcTest(DhoUserController.class)
@AutoConfigureMockMvc(addFilters = false)
class DhoUserControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DhoUserService service;

    @Test
    @DisplayName("Should return 200 and list of users")
    void findAll_Success() throws Exception {
        DhoUserDTO dto = new DhoUserDTO(1, "John Doe", "john@example.com", "ADMIN");

        when(service.findAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/dho-users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("John Doe"));
    }
}
