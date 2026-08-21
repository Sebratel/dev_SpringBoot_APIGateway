package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.BaseTest;
import br.com.sebratel.bff.dto.AffectedUserRequestDTO;
import br.com.sebratel.bff.dto.CreateImpactedUsersInputDTO;
import br.com.sebratel.bff.dto.massivas.ImpactedUsersOutputDTO;
import br.com.sebratel.bff.exceptions.ResourceNotFoundException;
import br.com.sebratel.bff.service.AffectedUserService;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
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

import static org.assertj.core.api.Assertions.assertThat;
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
    private AffectedUserService affectedUSerService;

    private Logger controllerLogger;
    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void attachLogAppender() {
        controllerLogger = (Logger) LoggerFactory.getLogger(AffectedUserController.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        controllerLogger.addAppender(logAppender);
    }

    @AfterEach
    void detachLogAppender() {
        controllerLogger.detachAppender(logAppender);
        logAppender.stop();
    }

    private ILoggingEvent lastEventContaining(String snippet) {
        return logAppender.list.stream()
                .filter(event -> event.getFormattedMessage().contains(snippet))
                .reduce((first, second) -> second)
                .orElseThrow(() -> new AssertionError("Nenhum log contendo: " + snippet));
    }

    @Test
    @DisplayName("Should return 200 when impacted users are found")
    void getAllImpactedUsers_Success() throws Exception {
        ImpactedUsersOutputDTO dto = ImpactedUsersOutputDTO.builder()
                .impactedUsers(List.of(Collections.emptyMap()))
                .build();

        when(affectedUSerService.getAll()).thenReturn(dto);

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

        when(affectedUSerService.getAll()).thenReturn(dto);

        mockMvc.perform(get("/api/v1/impacted-users"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("No impacted users found"));
    }

    @Test
    @DisplayName("Should return 404 when search fails in getAllImpactedUsers")
    void getAllImpactedUsers_Error() throws Exception {
        when(affectedUSerService.getAll()).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/v1/impacted-users"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Could not find users")));
    }

    @Test
    @DisplayName("Should return 201 when creating impacted users")
    void createImpactedUser_Success() throws Exception {
        ImpactedUsersOutputDTO dto = ImpactedUsersOutputDTO.builder()
                .impactedUsers(new ArrayList<>())
                .build();

        AffectedUserRequestDTO userDTO = AffectedUserRequestDTO.builder()
                .contractId(1L)
                .protocol(123L)
                .finishDate(LocalDateTime.now().plusHours(5))
                .build();

        CreateImpactedUsersInputDTO inputDTO = new CreateImpactedUsersInputDTO();
        inputDTO.setUsuarioAfetadoEntities(List.of(userDTO));
        inputDTO.setAssignmentId(1L);

        when(affectedUSerService.createImpactedUsersDTO(any())).thenReturn(dto);

        mockMvc.perform(post("/api/v1/impacted-users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Impacted user successfully created."));
    }

    @Test
    @DisplayName("Should return 400 when error occurs in createImpactedUser")
    void createImpactedUser_Error() throws Exception {
        AffectedUserRequestDTO userDTO = AffectedUserRequestDTO.builder()
                .contractId(1L)
                .protocol(123L)
                .finishDate(LocalDateTime.now().plusHours(5))
                .build();

        CreateImpactedUsersInputDTO inputDTO = new CreateImpactedUsersInputDTO();
        inputDTO.setUsuarioAfetadoEntities(List.of(userDTO));
        inputDTO.setAssignmentId(1L);

        when(affectedUSerService.createImpactedUsersDTO(any())).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(post("/api/v1/impacted-users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Should return 200 when searching by PPPoE and no users found")
    void getImpactedUsersByPppoe_Empty() throws Exception {
        ImpactedUsersOutputDTO dto = ImpactedUsersOutputDTO.builder()
                .impactedUsers(new ArrayList<>())
                .build();

        when(affectedUSerService.getUsuariosAfetadosByPppoe(anyString())).thenReturn(dto);

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

        when(affectedUSerService.getUsuariosAfetadosByPppoe(anyString())).thenReturn(dto);

        mockMvc.perform(get("/api/v1/impacted-users/pppoe/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Should return 404 when search fails in getImpactedUsersByPppoe")
    void getImpactedUsersByPppoe_Error() throws Exception {
        when(affectedUSerService.getUsuariosAfetadosByPppoe(anyString())).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/api/v1/impacted-users/pppoe/test"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Could not find users by PPPoE")));
    }

    @Test
    @DisplayName("Should return 200 when searching by contractId and no users found")
    void getImpactedUsersByContractId_Empty() throws Exception {
        ImpactedUsersOutputDTO dto = ImpactedUsersOutputDTO.builder()
                .impactedUsers(new ArrayList<>())
                .build();

        when(affectedUSerService.getUsuariosAfetadosByContractId(anyLong())).thenReturn(dto);

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

        when(affectedUSerService.getUsuariosAfetadosByContractId(anyLong())).thenReturn(dto);

        mockMvc.perform(get("/api/v1/impacted-users/contract/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Should return 404 when search fails in getImpactedUsersByContractId")
    void getImpactedUsersByContractId_Error() throws Exception {
        when(affectedUSerService.getUsuariosAfetadosByContractId(anyLong())).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/api/v1/impacted-users/contract/123"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Could not find users by contractId")));
    }

    @Test
    @DisplayName("Contrato sem massiva deve logar INFO sem stack trace (evita alerta falso do n8n)")
    void getImpactedUsersByContractId_NotFound_LogsInfoWithoutStackTrace() throws Exception {
        when(affectedUSerService.getUsuariosAfetadosByContractId(anyLong()))
                .thenThrow(new ResourceNotFoundException("Usuário afetado não encontrado"));

        mockMvc.perform(get("/api/v1/impacted-users/contract/283835"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Usuário afetado não encontrado")));

        ILoggingEvent event = lastEventContaining("Could not find users by contractId 283835");
        assertThat(event.getLevel()).isEqualTo(Level.INFO);
        assertThat(event.getThrowableProxy()).isNull();
    }

    @Test
    @DisplayName("Falha inesperada por contractId deve logar ERROR com stack trace")
    void getImpactedUsersByContractId_UnexpectedError_LogsErrorWithStackTrace() throws Exception {
        when(affectedUSerService.getUsuariosAfetadosByContractId(anyLong()))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/v1/impacted-users/contract/283835"))
                .andExpect(status().isNotFound());

        ILoggingEvent event = lastEventContaining("Error searching users by contractId 283835");
        assertThat(event.getLevel()).isEqualTo(Level.ERROR);
        assertThat(event.getThrowableProxy()).isNotNull();
        assertThat(event.getThrowableProxy().getClassName()).isEqualTo(RuntimeException.class.getName());
    }

    @Test
    @DisplayName("PPPoE sem massiva deve logar INFO sem stack trace")
    void getImpactedUsersByPppoe_NotFound_LogsInfoWithoutStackTrace() throws Exception {
        when(affectedUSerService.getUsuariosAfetadosByPppoe(anyString()))
                .thenThrow(new ResourceNotFoundException("Usuário afetado não encontrado"));

        mockMvc.perform(get("/api/v1/impacted-users/pppoe/test"))
                .andExpect(status().isNotFound());

        ILoggingEvent event = lastEventContaining("Could not find users by PPPoE test");
        assertThat(event.getLevel()).isEqualTo(Level.INFO);
        assertThat(event.getThrowableProxy()).isNull();
    }

    @Test
    @DisplayName("Falha inesperada por PPPoE deve logar ERROR com stack trace")
    void getImpactedUsersByPppoe_UnexpectedError_LogsErrorWithStackTrace() throws Exception {
        when(affectedUSerService.getUsuariosAfetadosByPppoe(anyString()))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/v1/impacted-users/pppoe/test"))
                .andExpect(status().isNotFound());

        ILoggingEvent event = lastEventContaining("Error searching users by PPPoE test");
        assertThat(event.getLevel()).isEqualTo(Level.ERROR);
        assertThat(event.getThrowableProxy()).isNotNull();
    }

    @Test
    @DisplayName("Should return 200 when searching by protocol")
    void getImpactedUsersByProtocol_Success() throws Exception {
        ImpactedUsersOutputDTO dto = ImpactedUsersOutputDTO.builder()
                .impactedUsers(List.of(Collections.emptyMap()))
                .build();

        when(affectedUSerService.getUsuariosByProtocol(anyLong())).thenReturn(dto);

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

        when(affectedUSerService.getUsuariosByProtocol(anyLong())).thenReturn(dto);

        mockMvc.perform(get("/api/v1/impacted-users/protocol/123"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Should return 404 when search fails in getImpactedUsersByProtocol")
    void getImpactedUsersByProtocol_Error() throws Exception {
        when(affectedUSerService.getUsuariosByProtocol(anyLong())).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/api/v1/impacted-users/protocol/123"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Could not find users by protocol")));
    }

    @Test
    @DisplayName("Protocolo sem massiva deve logar INFO sem stack trace")
    void getImpactedUsersByProtocol_NotFound_LogsInfoWithoutStackTrace() throws Exception {
        when(affectedUSerService.getUsuariosByProtocol(anyLong()))
                .thenThrow(new ResourceNotFoundException("Nenhum usuário afetado encontrado para o protocolo: 123"));

        mockMvc.perform(get("/api/v1/impacted-users/protocol/123"))
                .andExpect(status().isNotFound());

        ILoggingEvent event = lastEventContaining("Could not find users by protocol 123");
        assertThat(event.getLevel()).isEqualTo(Level.INFO);
        assertThat(event.getThrowableProxy()).isNull();
    }

    @Test
    @DisplayName("Falha inesperada por protocolo deve logar ERROR com stack trace")
    void getImpactedUsersByProtocol_UnexpectedError_LogsErrorWithStackTrace() throws Exception {
        when(affectedUSerService.getUsuariosByProtocol(anyLong()))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/v1/impacted-users/protocol/123"))
                .andExpect(status().isNotFound());

        ILoggingEvent event = lastEventContaining("Error searching users by protocol 123");
        assertThat(event.getLevel()).isEqualTo(Level.ERROR);
        assertThat(event.getThrowableProxy()).isNotNull();
    }

    @Test
    @DisplayName("Listagem geral sem resultado deve logar INFO sem stack trace")
    void getAllImpactedUsers_NotFound_LogsInfoWithoutStackTrace() throws Exception {
        when(affectedUSerService.getAll())
                .thenThrow(new ResourceNotFoundException("Usuário afetado não encontrado"));

        mockMvc.perform(get("/api/v1/impacted-users"))
                .andExpect(status().isNotFound());

        ILoggingEvent event = lastEventContaining("Could not find users: Usuário afetado não encontrado");
        assertThat(event.getLevel()).isEqualTo(Level.INFO);
        assertThat(event.getThrowableProxy()).isNull();
    }

    @Test
    @DisplayName("Falha inesperada na listagem geral deve logar ERROR com stack trace")
    void getAllImpactedUsers_UnexpectedError_LogsErrorWithStackTrace() throws Exception {
        when(affectedUSerService.getAll()).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/v1/impacted-users"))
                .andExpect(status().isNotFound());

        ILoggingEvent event = lastEventContaining("Error searching impacted users");
        assertThat(event.getLevel()).isEqualTo(Level.ERROR);
        assertThat(event.getThrowableProxy()).isNotNull();
    }

    @Test
    @DisplayName("Should return 200 when removing users by protocol")
    void removeUsersByProtocol_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/impacted-users/protocol/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Should return 404 when error occurs in removeUsersByProtocol")
    void removeUsersByProtocol_Error() throws Exception {
        doThrow(new RuntimeException("Error")).when(affectedUSerService).removeUsersByProtocol(anyLong());

        mockMvc.perform(delete("/api/v1/impacted-users/protocol/123"))
                .andExpect(status().isNotFound())
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
    @DisplayName("Should return 400 when error occurs in updateFinishDateByProtocol")
    void updateFinishDateByProtocol_Error() throws Exception {
        doThrow(new RuntimeException("Error")).when(affectedUSerService).changeEstimationTime(anyLong(), any());

        mockMvc.perform(patch("/api/v1/impacted-users/protocol/123")
                        .param("finishDate", LocalDateTime.now().toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
