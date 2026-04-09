package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.InactivateAccountDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InactivateAccountProducerTest {

    @Mock
    private KafkaTemplate<String, InactivateAccountDTO> kafkaTemplate;

    @InjectMocks
    private InactivateAccountProducer producer;

    @Test
    @DisplayName("Should send inactivation event with correct userInfo as key")
    void sendInactivationEvent_Success() {
        InactivateAccountDTO.InactivateAccountUserInfo userInfo = InactivateAccountDTO.InactivateAccountUserInfo.builder()
                .name("John Doe")
                .cpf("123.456.789-00")
                .build();

        InactivateAccountDTO event = InactivateAccountDTO.builder()
                .userInfo(userInfo)
                .build();

        producer.sendInactivationEvent(event);

        String expectedKey = "John Doe - 123.456.789-00";
        verify(kafkaTemplate).send(eq("inactivate-account-topic"), eq(expectedKey), eq(event));
    }

    @Test
    @DisplayName("Should send inactivation event with 'unknown' key when userInfo is null")
    void sendInactivationEvent_NullUserInfo() {
        InactivateAccountDTO event = InactivateAccountDTO.builder()
                .userInfo(null)
                .build();

        producer.sendInactivationEvent(event);

        verify(kafkaTemplate).send(eq("inactivate-account-topic"), eq("unknown"), eq(event));
    }
}
