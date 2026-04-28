package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.BaseTest;
import br.com.sebratel.bff.controller.scripts.ConsumptionController;
import br.com.sebratel.bff.dto.ConsumoDTO;
import br.com.sebratel.bff.service.ConsumoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConsumptionController.class)
@AutoConfigureMockMvc(addFilters = false)
class ConsumptionControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConsumoService consumoService;

    @Test
    @DisplayName("Should return 200 when listing high consumption")
    void getHighConsumption_Success() throws Exception {
        ConsumoDTO dto = new ConsumoDTO("String", "String", "String", "String", 2.0, 3.0, 4.0);
        List<ConsumoDTO> list = List.of(dto);

        when(consumoService.listarConsumoAlto()).thenReturn(list);

        mockMvc.perform(get("/api/v1/high-consumption")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Should return 200 when listing paged high consumption")
    void getHighConsumptionPaged_Success() throws Exception {
        ConsumoDTO dto = new ConsumoDTO("String", "String", "String", "String", 2.0, 3.0, 4.0);
        Page<ConsumoDTO> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);

        when(consumoService.listarConsumoAltoPaginado(anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/api/v1/high-consumption-paged")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}
