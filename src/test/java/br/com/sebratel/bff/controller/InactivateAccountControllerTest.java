package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.BaseTest;
import br.com.sebratel.bff.controller.scripts.InactivateAccountController;
import br.com.sebratel.bff.dto.InactivateAccountDTO;
import br.com.sebratel.bff.service.InactivateAccountProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InactivateAccountController.class)
@AutoConfigureMockMvc(addFilters = false)
class InactivateAccountControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InactivateAccountProducer producer;

    @Test
    @DisplayName("Should return 202 when inactivation event is sent successfully")
    void inactivateAccount_Success() throws Exception {
        InactivateAccountDTO.InactivateAccountUserInfo userInfo = InactivateAccountDTO.InactivateAccountUserInfo.builder()
                .name("Test User")
                .cpf("12345678900")
                .build();
        
        InactivateAccountDTO request = InactivateAccountDTO.builder()
                .userInfo(userInfo)
                .build();

        mockMvc.perform(post("/api/v1/inactivate-account")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Inactivation process successfully queued."));

        verify(producer).sendInactivationEvent(any(InactivateAccountDTO.class));
    }

    @Test
    @DisplayName("Should return 500 when inactivation event fails to send")
    void inactivateAccount_Error() throws Exception {
        InactivateAccountDTO request = InactivateAccountDTO.builder().build();

        doThrow(new RuntimeException("Could not add to the queue: Kafka down")).when(producer).sendInactivationEvent(any());

        mockMvc.perform(post("/api/v1/inactivate-account")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Error: Could not add to the queue: Kafka down")));
    }
}
