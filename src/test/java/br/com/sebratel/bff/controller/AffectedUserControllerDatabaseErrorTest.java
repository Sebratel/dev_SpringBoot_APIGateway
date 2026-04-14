package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.ApiResponse;
import br.com.sebratel.bff.dto.CreateImpactedUsersInputDTO;
import br.com.sebratel.bff.service.AffectedUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AffectedUserController.class)
@AutoConfigureMockMvc(addFilters = false)
class AffectedUserControllerDatabaseErrorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AffectedUserService affectedUSerService;

    @Test
    @DisplayName("Should return 400 and database errors when a DB integrity error occurs on creation")
    void createImpactedUser_DatabaseError() throws Exception {
        CreateImpactedUsersInputDTO inputDTO = new CreateImpactedUsersInputDTO();
        inputDTO.setUsuarioAfetadoEntities(Collections.emptyList());
        inputDTO.setAssignmentId(1L);

        String dbError = "Column 'created_by' cannot be null";
        when(affectedUSerService.createImpactedUsersDTO(any())).thenThrow(new RuntimeException(dbError));

        mockMvc.perform(post("/api/v1/impacted-users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors.database.created_by").value("cannot be null"));
    }
}
