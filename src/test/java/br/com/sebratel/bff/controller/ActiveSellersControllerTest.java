package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.BaseTest;
import br.com.sebratel.bff.controller.scripts.ActiveSellersController;
import br.com.sebratel.bff.dto.VendedoresAtivosDTO;
import br.com.sebratel.bff.service.VendedoresAtivosService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ActiveSellersController.class)
@AutoConfigureMockMvc(addFilters = false)
class ActiveSellersControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VendedoresAtivosService vendedoresAtivosService;

    @Test
    @DisplayName("Should return 200 when listing active sellers")
    void getActiveSellers_Success() throws Exception {
        VendedoresAtivosDTO dto = new VendedoresAtivosDTO(
                "", ""
        );
        List<VendedoresAtivosDTO> list = List.of(dto);

        when(vendedoresAtivosService.listarVendedoresAtivos()).thenReturn(list);

        mockMvc.perform(get("/api/v1/sellers/active")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
