package br.com.sebratel.bff.cucumber;

import br.com.sebratel.bff.dto.QrCodeInputDTO;
import br.com.sebratel.bff.dto.QrCodeOutputDTO;
import br.com.sebratel.bff.service.QrCodeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class QrCodeStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private QrCodeService qrCodeService;

    @Given("the QrCode service is ready")
    public void the_qr_code_service_is_ready() throws Exception {
        when(qrCodeService.gerarQrCodeParaFuncionario(any())).thenReturn(new QrCodeOutputDTO());
    }

    @When("I request to generate QrCode with data {string}")
    public void i_request_to_generate_qr_code_with_data(String data) throws Exception {
        QrCodeInputDTO request = new QrCodeInputDTO();
        request.setJson(data);
        ResultActions resultActions = mockMvc.perform(get("/api/v1/qr-code/gerar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
        CommonStepDefinitions.setResultActions(resultActions);
    }
}
