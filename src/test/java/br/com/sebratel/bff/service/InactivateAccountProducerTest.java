package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.InactivateAccountDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(CompletableFuture.completedFuture(null));

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

        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(CompletableFuture.completedFuture(null));

        producer.sendInactivationEvent(event);

        verify(kafkaTemplate).send(eq("inactivate-account-topic"), eq("unknown"), eq(event));
    }

    @Test
    @DisplayName("Should throw exception when Kafka send fails")
    void sendInactivationEvent_KafkaFailure() {
        InactivateAccountDTO event = InactivateAccountDTO.builder().build();

        CompletableFuture<Object> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka down"));
        
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn((CompletableFuture) future);

        assertThrows(RuntimeException.class, () -> producer.sendInactivationEvent(event));
    }
}
