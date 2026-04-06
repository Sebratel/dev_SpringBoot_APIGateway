package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.BaseTest;
import br.com.sebratel.bff.dto.AuthenticationSitesOutputDTO;
import br.com.sebratel.bff.service.AuthenticationSitesService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticationSitesController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthenticationSitesControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationSitesService authenticationSitesService;

    @Test
    @DisplayName("Should return 200 and list of sites when title is provided")
    void getSites_Success() throws Exception {
        AuthenticationSitesOutputDTO dto = new AuthenticationSitesOutputDTO("City", "Neighborhood");
        when(authenticationSitesService.execute(anyString())).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/sites")
                        .param("title", "test")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Sites retrivied sucessfully"))
                .andExpect(jsonPath("$.data").isArray());
    }
}
