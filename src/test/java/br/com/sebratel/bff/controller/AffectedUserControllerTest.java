package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.BaseTest;
import br.com.sebratel.bff.dto.CreateImpactedUsersInputDTO;
import br.com.sebratel.bff.dto.massivas.ImpactedUsersOutputDTO;
import br.com.sebratel.bff.service.UsuarioAfetadoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AffectedUserController.class)
@AutoConfigureMockMvc(addFilters = false)
class AffectedUserControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UsuarioAfetadoService usuarioAfetadoService;

    @Test
    @DisplayName("Should return 200 when impacted users are found")
    void getAllImpactedUsers_Success() throws Exception {
        ImpactedUsersOutputDTO dto = ImpactedUsersOutputDTO.builder()
                .impactedUsers(List.of(Collections.emptyMap()))
                .build();

        when(usuarioAfetadoService.getAll()).thenReturn(dto);

        mockMvc.perform(get("/api/v1/impacted-users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Impacted users successfully found."));
    }

    @Test
    @DisplayName("Should return 404 when no impacted users are found")
    void getAllImpactedUsers_NotFound() throws Exception {
        ImpactedUsersOutputDTO dto = ImpactedUsersOutputDTO.builder()
                .impactedUsers(new ArrayList<>())
                .build();

        when(usuarioAfetadoService.getAll()).thenReturn(dto);

        mockMvc.perform(get("/api/v1/impacted-users"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("No impacted users found"));
    }

    @Test
    @DisplayName("Should return 500 when error occurs in getAllImpactedUsers")
    void getAllImpactedUsers_Error() throws Exception {
        when(usuarioAfetadoService.getAll()).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/v1/impacted-users"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Error searching for users")));
    }

    @Test
    @DisplayName("Should return 201 when creating impacted users")
    void createImpactedUser_Success() throws Exception {
        ImpactedUsersOutputDTO dto = ImpactedUsersOutputDTO.builder()
                .impactedUsers(new ArrayList<>())
                .build();

        CreateImpactedUsersInputDTO inputDTO = new CreateImpactedUsersInputDTO();
        inputDTO.setUsuarioAfetadoEntities(new ArrayList<>());
        inputDTO.setAssignmentId(1L);

        when(usuarioAfetadoService.createImpactedUsersDTO(any())).thenReturn(dto);

        mockMvc.perform(post("/api/v1/impacted-users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Impacted user successfully created."));
    }

    @Test
    @DisplayName("Should return 500 when error occurs in createImpactedUser")
    void createImpactedUser_Error() throws Exception {
        CreateImpactedUsersInputDTO inputDTO = new CreateImpactedUsersInputDTO();
        inputDTO.setUsuarioAfetadoEntities(new ArrayList<>());
        inputDTO.setAssignmentId(1L);

        when(usuarioAfetadoService.createImpactedUsersDTO(any())).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(post("/api/v1/impacted-users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Should return 200 when searching by PPPoE and no users found")
    void getImpactedUsersByPppoe_Empty() throws Exception {
        ImpactedUsersOutputDTO dto = ImpactedUsersOutputDTO.builder()
                .impactedUsers(new ArrayList<>())
                .build();

        when(usuarioAfetadoService.getUsuariosAfetadosByPppoe(anyString())).thenReturn(dto);

        mockMvc.perform(get("/api/v1/impacted-users/pppoe/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Should return 200 when searching by PPPoE and users found")
    void getImpactedUsersByPppoe_Found() throws Exception {
        ImpactedUsersOutputDTO dto = ImpactedUsersOutputDTO.builder()
                .impactedUsers(List.of(Collections.emptyMap()))
                .build();

        when(usuarioAfetadoService.getUsuariosAfetadosByPppoe(anyString())).thenReturn(dto);

        mockMvc.perform(get("/api/v1/impacted-users/pppoe/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Should return 500 when error occurs in getImpactedUsersByPppoe")
    void getImpactedUsersByPppoe_Error() throws Exception {
        when(usuarioAfetadoService.getUsuariosAfetadosByPppoe(anyString())).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/api/v1/impacted-users/pppoe/test"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Should return 200 when searching by contractId and no users found")
    void getImpactedUsersByContractId_Empty() throws Exception {
        ImpactedUsersOutputDTO dto = ImpactedUsersOutputDTO.builder()
                .impactedUsers(new ArrayList<>())
                .build();

        when(usuarioAfetadoService.getUsuariosAfetadosByContractId(anyLong())).thenReturn(dto);

        mockMvc.perform(get("/api/v1/impacted-users/contract/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Should return 200 when searching by contractId and users found")
    void getImpactedUsersByContractId_Found() throws Exception {
        ImpactedUsersOutputDTO dto = ImpactedUsersOutputDTO.builder()
                .impactedUsers(List.of(Collections.emptyMap()))
                .build();

        when(usuarioAfetadoService.getUsuariosAfetadosByContractId(anyLong())).thenReturn(dto);

        mockMvc.perform(get("/api/v1/impacted-users/contract/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Should return 500 when error occurs in getImpactedUsersByContractId")
    void getImpactedUsersByContractId_Error() throws Exception {
        when(usuarioAfetadoService.getUsuariosAfetadosByContractId(anyLong())).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/api/v1/impacted-users/contract/123"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Should return 200 when searching by protocol")
    void getImpactedUsersByProtocol_Success() throws Exception {
        ImpactedUsersOutputDTO dto = ImpactedUsersOutputDTO.builder()
                .impactedUsers(List.of(Collections.emptyMap()))
                .build();

        when(usuarioAfetadoService.getUsuariosByProtocol(anyLong())).thenReturn(dto);

        mockMvc.perform(get("/api/v1/impacted-users/protocol/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Should return 404 when no users found by protocol")
    void getImpactedUsersByProtocol_NotFound() throws Exception {
        ImpactedUsersOutputDTO dto = ImpactedUsersOutputDTO.builder()
                .impactedUsers(new ArrayList<>())
                .build();

        when(usuarioAfetadoService.getUsuariosByProtocol(anyLong())).thenReturn(dto);

        mockMvc.perform(get("/api/v1/impacted-users/protocol/123"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Should return 500 when error occurs in getImpactedUsersByProtocol")
    void getImpactedUsersByProtocol_Error() throws Exception {
        when(usuarioAfetadoService.getUsuariosByProtocol(anyLong())).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/api/v1/impacted-users/protocol/123"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Should return 200 when removing users by protocol")
    void removeUsersByProtocol_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/impacted-users/protocol/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Should return 500 when error occurs in removeUsersByProtocol")
    void removeUsersByProtocol_Error() throws Exception {
        doThrow(new RuntimeException("Error")).when(usuarioAfetadoService).removeUsersByProtocol(anyLong());

        mockMvc.perform(delete("/api/v1/impacted-users/protocol/123"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Should return 200 when updating finish date")
    void updateFinishDateByProtocol_Success() throws Exception {
        mockMvc.perform(patch("/api/v1/impacted-users/protocol/123")
                        .param("finishDate", LocalDateTime.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Should return 500 when error occurs in updateFinishDateByProtocol")
    void updateFinishDateByProtocol_Error() throws Exception {
        doThrow(new RuntimeException("Error")).when(usuarioAfetadoService).alterarDataEstimadaParaFinalizacao(anyLong(), any());

        mockMvc.perform(patch("/api/v1/impacted-users/protocol/123")
                        .param("finishDate", LocalDateTime.now().toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }
}
