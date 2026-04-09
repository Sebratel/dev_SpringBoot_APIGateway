package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.InactivateAccountDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InactivateAccountProducer {

    private final KafkaTemplate<String, InactivateAccountDTO> kafkaTemplate;
    private static final String TOPIC = "inactivate-account-topic";

    public void sendInactivationEvent(InactivateAccountDTO event) {
        String userInfo = event.getUserInfo() != null ? event.getUserInfo().getName() + " - " + event.getUserInfo().getCpf() : "unknown";
        log.info("Sending inactivation event for: {}", userInfo);
        kafkaTemplate.send(TOPIC, userInfo, event);
    }
}
