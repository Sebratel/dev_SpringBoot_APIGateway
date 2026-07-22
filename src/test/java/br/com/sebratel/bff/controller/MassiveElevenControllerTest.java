package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.BaseTest;
import br.com.sebratel.bff.dto.massivas.*;
import br.com.sebratel.bff.dto.massivas.api.*;
import br.com.sebratel.bff.service.massivas.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MassiveElevenController.class)
@AutoConfigureMockMvc(addFilters = false)
class MassiveElevenControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdicionarMassivaNoEllevenService adicionarMassivaNoEllevenService;

    @MockitoBean
    private AdicionarMassivaNoEllevenApiService adicionarMassivaNoEllevenApiService;

    @MockitoBean
    private EnviarListaDeAfetadosParaNativeService enviarListaDeAfetadosParaNativeService;

    @MockitoBean
    private GetAllMassivesService getAllMassivesService;

    @MockitoBean
    private RecuperarTodasAsMassivasPeloBancoService recuperarTodasAsMassivasPeloBancoService;

    @MockitoBean
    private RecuperarPrevisaoMassivaPorContratoService recuperarPrevisaoMassivaPorContratoService;

    @MockitoBean
    private FinalizarMassivaNoEllevenApiService finalizarMassivaNoEllevenApiService;

    @Test
    @DisplayName("Should create massive incident with flutter data")
    void createMassiveIncidentWithFlutterData_Success() throws Exception {
        String json = "{\n" +
                "  \"startDate\": \"06/04/2026\",\n" +
                "  \"startTime\": \"10:00\",\n" +
                "  \"accessPointIds\": [1],\n" +
                "  \"assignmentDescription\": \"Test Description\",\n" +
                "  \"maintenanceDate\": \"06/04/2026\",\n" +
                "  \"maintenanceTime\": \"11:00\",\n" +
                "  \"cookieString\": \"test-cookie\"\n" +
                "}";

        CriacaoDeMassivaOutputDTO output = CriacaoDeMassivaOutputDTO.builder()
                .id("1")
                .build();

        when(adicionarMassivaNoEllevenService.salvarNoBancoERP(any())).thenReturn(output);

        mockMvc.perform(post("/api/v1/massive-incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Should retrieve all massive incidents from Elleven and handle non-null response")
    void retrieveAllMassiveIncidents_NonNullResponse() throws Exception {
        EllevenApiResponseDTO output = new EllevenApiResponseDTO();
        ResponseDataDTO responseData = new ResponseDataDTO();
        responseData.setTotalRecords(10);
        output.setResponse(responseData);

        when(getAllMassivesService.getAllSolicitations()).thenReturn(output);

        mockMvc.perform(get("/api/v1/massive-incidents/getAllMassives"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Should retrieve all massive incidents from Elleven and handle null response")
    void retrieveAllMassiveIncidents_NullResponse() throws Exception {
        EllevenApiResponseDTO output = new EllevenApiResponseDTO();
        output.setResponse(null);

        when(getAllMassivesService.getAllSolicitations()).thenReturn(output);

        mockMvc.perform(get("/api/v1/massive-incidents/getAllMassives"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Should retrieve all massive incidents via database")
    void retrieveAllMassiveIncidentsViaDatabase_Success() throws Exception {
        List<MassivasBFFOutputDTO> output = new ArrayList<>();

        when(recuperarTodasAsMassivasPeloBancoService.executar()).thenReturn(output);

        mockMvc.perform(get("/api/v1/massive-incidents/recover-via-database"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("Should create massive incident via API")
    void createMassiveIncidentViaApi_Success() throws Exception {
        AberturaRegistroMassivoInputDTO input = new AberturaRegistroMassivoInputDTO();
        input.setIncidentStatusId(1);
        input.setPersonId(1L);
        input.setIncidentTypeId(1);
        input.setCatalogServiceId(1);
        input.setServiceLevelAgreementId(1);
        input.setMatrixType(1);
        input.setTeamCode("TEAM");
        input.setSolicitationServiceCategory1("CAT1");
        input.setAssignment(new AberturaRegistroMassivoAssignmentDTO());

        AberturaRegistroMassivoOutputDTO output = new AberturaRegistroMassivoOutputDTO();
        output.setSuccess(true);
        AberturaRegistroMassivoResponseDTO responseDTO = new AberturaRegistroMassivoResponseDTO();
        responseDTO.setProtocol(123L);
        output.setResponse(responseDTO);

        when(adicionarMassivaNoEllevenApiService.executar(any())).thenReturn(output);

        mockMvc.perform(post("/api/v1/massive-incidents/save-massive-via-api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Should return 502 when create via API fails on Elleven side")
    void createMassiveIncidentViaApi_Fail() throws Exception {
        AberturaRegistroMassivoInputDTO input = new AberturaRegistroMassivoInputDTO();
        input.setIncidentStatusId(1);
        input.setPersonId(1L);
        input.setIncidentTypeId(1);
        input.setCatalogServiceId(1);
        input.setServiceLevelAgreementId(1);
        input.setMatrixType(1);
        input.setTeamCode("TEAM");
        input.setSolicitationServiceCategory1("CAT1");
        input.setAssignment(new AberturaRegistroMassivoAssignmentDTO());

        AberturaRegistroMassivoOutputDTO output = new AberturaRegistroMassivoOutputDTO();
        output.setSuccess(false);

        when(adicionarMassivaNoEllevenApiService.executar(any())).thenReturn(output);

        mockMvc.perform(post("/api/v1/massive-incidents/save-massive-via-api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Should finalize massive incident via API")
    void finalizeMassiveIncidentViaApi_Success() throws Exception {
        FinalizaRegistroMassivoInputDTO input = new FinalizaRegistroMassivoInputDTO();
        FinalizarRegistroMassivoOutputDTO output = FinalizarRegistroMassivoOutputDTO.builder()
                .success(true)
                .build();

        when(finalizarMassivaNoEllevenApiService.executar(any())).thenReturn(output);

        mockMvc.perform(delete("/api/v1/massive-incidents/finalize-ticket-via-api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Should return 502 when finalization fails on Elleven side")
    void finalizeMassiveIncidentViaApi_Fail() throws Exception {
        FinalizaRegistroMassivoInputDTO input = new FinalizaRegistroMassivoInputDTO();
        FinalizarRegistroMassivoOutputDTO output = FinalizarRegistroMassivoOutputDTO.builder()
                .success(false)
                .build();

        when(finalizarMassivaNoEllevenApiService.executar(any())).thenReturn(output);

        mockMvc.perform(delete("/api/v1/massive-incidents/finalize-ticket-via-api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Should return 500 when creating massive incident fails with exception")
    void createMassiveIncidentWithFlutterData_Error() throws Exception {
        String json = "{\n" +
                "  \"startDate\": \"06/04/2026\",\n" +
                "  \"startTime\": \"10:00\",\n" +
                "  \"accessPointIds\": [1],\n" +
                "  \"assignmentDescription\": \"Test Description\",\n" +
                "  \"maintenanceDate\": \"06/04/2026\",\n" +
                "  \"maintenanceTime\": \"11:00\",\n" +
                "  \"cookieString\": \"test-cookie\"\n" +
                "}";

        when(adicionarMassivaNoEllevenService.salvarNoBancoERP(any())).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(post("/api/v1/massive-incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should return 500 when retrieving via database fails")
    void retrieveAllMassiveIncidentsViaDatabase_Error() throws Exception {
        when(recuperarTodasAsMassivasPeloBancoService.executar()).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/api/v1/massive-incidents/recover-via-database"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should return 500 when creating via API fails with exception")
    void createMassiveIncidentViaApi_Exception() throws Exception {
        AberturaRegistroMassivoInputDTO input = new AberturaRegistroMassivoInputDTO();
        input.setIncidentStatusId(1);
        input.setPersonId(1L);
        input.setIncidentTypeId(1);
        input.setCatalogServiceId(1);
        input.setServiceLevelAgreementId(1);
        input.setMatrixType(1);
        input.setTeamCode("TEAM");
        input.setSolicitationServiceCategory1("CAT1");
        input.setAssignment(new AberturaRegistroMassivoAssignmentDTO());

        when(adicionarMassivaNoEllevenApiService.executar(any())).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(post("/api/v1/massive-incidents/save-massive-via-api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should return 500 when finalization fails with exception")
    void finalizeMassiveIncidentViaApi_Exception() throws Exception {
        FinalizaRegistroMassivoInputDTO input = new FinalizaRegistroMassivoInputDTO();

        when(finalizarMassivaNoEllevenApiService.executar(any())).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(delete("/api/v1/massive-incidents/finalize-ticket-via-api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.messages[0].message").value("Internal server error while finalizing massive incident: Error"));
    }

    @Test
    @DisplayName("Should return 502 when finalization fails due to linked protocol error")
    void finalizeMassiveIncidentViaApi_LinkedProtocolError() throws Exception {
        FinalizaRegistroMassivoInputDTO input = new FinalizaRegistroMassivoInputDTO();

        when(finalizarMassivaNoEllevenApiService.executar(any())).thenThrow(new RuntimeException("Failed to finalize linked protocol: 123"));

        mockMvc.perform(delete("/api/v1/massive-incidents/finalize-ticket-via-api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.messages[0].message").value("Internal server error while finalizing massive incident: Failed to finalize linked protocol: 123"));
    }
}
