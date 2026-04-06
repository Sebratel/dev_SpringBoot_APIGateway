package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.BaseTest;
import br.com.sebratel.bff.dto.AquisicaoDTO;
import br.com.sebratel.bff.service.AquisicaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AcquisitionController.class)
@AutoConfigureMockMvc(addFilters = false)
class AcquisitionControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AquisicaoService aquisicaoService;

    @BeforeEach
    void setUp() {
        AquisicaoDTO dto = new AquisicaoDTO(1L, "", "", LocalDate.now(), "String", LocalDate.now(), 2.0, "3.0","String", "String");
        List<AquisicaoDTO> list = List.of(dto);
        when(aquisicaoService.listarAquisicoes()).thenReturn(list);
    }

    @Test
    @DisplayName("Should return 200 using the new English route")
    void getAcquisitions_NewRoute_Success() throws Exception {
        mockMvc.perform(get("/api/v1/acquisitions/recover-acquisition-orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Should return 200 using the old Portuguese route")
    void getAcquisitions_OldRoute_Success() throws Exception {
        mockMvc.perform(get("/api/v1/aquisicoes/recover-acquisition-orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$").isArray());
    }
}
