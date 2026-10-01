package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.BaseTest;
import br.com.sebratel.bff.dto.massivas.api.AberturaRegistroMassivoAssignmentDTO;
import br.com.sebratel.bff.dto.massivas.api.AberturaRegistroMassivoInputDTO;
import br.com.sebratel.bff.dto.massivas.api.AberturaRegistroMassivoOutputDTO;
import br.com.sebratel.bff.dto.massivas.api.AberturaRegistroMassivoResponseDTO;
import br.com.sebratel.bff.service.gmud.AbrirGmudNoEllevenApiService;
import br.com.sebratel.bff.service.massivas.MassivaIdempotencyStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GmudEllevenController.class)
@Import(MassivaIdempotencyStore.class)
@AutoConfigureMockMvc(addFilters = false)
class GmudEllevenControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AbrirGmudNoEllevenApiService abrirGmudNoEllevenApiService;

    private AberturaRegistroMassivoInputDTO buildGmudInput() {
        AberturaRegistroMassivoInputDTO input = new AberturaRegistroMassivoInputDTO();
        input.setIncidentStatusId(1);
        input.setPersonId(30611L);
        input.setIncidentTypeId(1084);
        input.setCatalogServiceId(1082);
        input.setServiceLevelAgreementId(99);
        input.setMatrixType(2);
        input.setTeamCode("2.8");
        input.setSolicitationServiceCategory1("infra - 42");
        AberturaRegistroMassivoAssignmentDTO assignment = new AberturaRegistroMassivoAssignmentDTO();
        assignment.setTitle("GMUD - Teste");
        assignment.setDescription("Descrição da mudança");
        input.setAssignment(assignment);
        return input;
    }

    @Test
    @DisplayName("Should open GMUD via API")
    void abrirGmudViaApi_Success() throws Exception {
        AberturaRegistroMassivoOutputDTO output = new AberturaRegistroMassivoOutputDTO();
        output.setSuccess(true);
        AberturaRegistroMassivoResponseDTO responseDTO = new AberturaRegistroMassivoResponseDTO();
        responseDTO.setProtocol(1900000L);
        responseDTO.setAssignmentId(999L);
        output.setResponse(responseDTO);

        when(abrirGmudNoEllevenApiService.executar(any())).thenReturn(output);

        mockMvc.perform(post("/api/v1/gmud/abrir-via-api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildGmudInput())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.response.protocol").value(1900000L));
    }

    @Test
    @DisplayName("Should return 502 when GMUD open fails on Elleven side")
    void abrirGmudViaApi_Fail() throws Exception {
        AberturaRegistroMassivoOutputDTO output = new AberturaRegistroMassivoOutputDTO();
        output.setSuccess(false);

        when(abrirGmudNoEllevenApiService.executar(any())).thenReturn(output);

        mockMvc.perform(post("/api/v1/gmud/abrir-via-api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildGmudInput())))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Should return 500 when GMUD open fails with exception")
    void abrirGmudViaApi_Exception() throws Exception {
        when(abrirGmudNoEllevenApiService.executar(any())).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(post("/api/v1/gmud/abrir-via-api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildGmudInput())))
                .andExpect(status().isInternalServerError());
    }
}
