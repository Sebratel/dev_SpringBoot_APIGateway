package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.InactivateAccountDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
@Slf4j
@Service
@RequiredArgsConstructor
public class InactivateAccountProducer {

    private final KafkaTemplate<String, InactivateAccountDTO> kafkaTemplate;
    private static final String TOPIC = "inactivate-account-topic";

    public void sendInactivationEvent(InactivateAccountDTO event) {
        String userInfo = event.getUserInfo() != null ? event.getUserInfo().getName() + " - " + event.getUserInfo().getCpf() : "unknown";
        log.info("Sending inactivation event for: {}", userInfo);
        try {
            kafkaTemplate.send(TOPIC, userInfo, event).get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Failed to send inactivation event to Kafka", e);
            throw new RuntimeException("Could not add to the queue: " + e.getMessage(), e);
        }
    }
}
