package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.InactivateAccountDTO;
import br.com.sebratel.bff.model.entity.InactivateAccountEntity;
import br.com.sebratel.bff.repository.afetados.InactivateAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InactivateAccountConsumerTest {

    @Mock
    private InactivateAccountRepository repository;

    @InjectMocks
    private InactivateAccountConsumer consumer;

    @Test
    @DisplayName("Should save inactivation record when event is received")
    void consume_Success() {
        InactivateAccountDTO.InactivateAccountUserInfo userInfo = InactivateAccountDTO.InactivateAccountUserInfo.builder()
                .name("Jane Doe")
                .cpf("000.000.000-00")
                .build();

        LocalDateTime now = LocalDateTime.now();
        InactivateAccountDTO event = InactivateAccountDTO.builder()
                .userInfo(userInfo)
                .statusChangedDate(now)
                .inactivationDate(now.plusDays(1))
                .build();

        consumer.consume(event);

        ArgumentCaptor<InactivateAccountEntity> captor = ArgumentCaptor.forClass(InactivateAccountEntity.class);
        verify(repository).save(captor.capture());

        InactivateAccountEntity saved = captor.getValue();
        assertEquals("Jane Doe", saved.getName());
        assertEquals("000.000.000-00", saved.getCpf());
        assertEquals(now, saved.getStatusChangedDate());
        assertEquals(now.plusDays(1), saved.getInactivationDate());
        assertNotNull(saved.getCreated());
    }

    @Test
    @DisplayName("Should not save when event or userInfo is null")
    void consume_NullEvent() {
        consumer.consume(null);
        verify(repository, never()).save(any());

        InactivateAccountDTO event = new InactivateAccountDTO();
        consumer.consume(event);
        verify(repository, never()).save(any());
    }
}
