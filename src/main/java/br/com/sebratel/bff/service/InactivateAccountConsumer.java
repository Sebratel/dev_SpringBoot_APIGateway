package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.InactivateAccountDTO;
import br.com.sebratel.bff.model.entity.InactivateAccountEntity;
import br.com.sebratel.bff.repository.afetados.InactivateAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class InactivateAccountConsumer {

    private final InactivateAccountRepository repository;

    @KafkaListener(topics = "inactivate-account-topic", groupId = "bff-group")
    public void consume(InactivateAccountDTO event) {
        log.info("Received inactivation event for account: [{} | {}", event.getUserInfo().getCpf(), event.getUserInfo().getCpf());

        if (event.getStatusChangedDate() != null) {
           return;
        }

        // TODO: use employee service to find tx_id and name
        //  it should recover only one register
        //  if its present then should continue
        //  then get people id
        event.setAccountId("1234");


        InactivateAccountEntity entity = InactivateAccountEntity.builder()
                .accountId(event.getAccountId() != null ? Long.parseLong(event.getAccountId()) : null)
                .name(event.getUserInfo() != null ? event.getUserInfo().getName() : null)
                .cpf(event.getUserInfo() != null ? event.getUserInfo().getCpf() : null)
                .created(LocalDateTime.now())
                .build();

        repository.save(entity);
        log.info("Inactivation record saved for account: {}", event.getAccountId());
    }

    private boolean setStatusToRemoved(InactivateAccountDTO event) {
        try {
            log.info("Calling remove_status.py");
            ProcessBuilder processBuilder = new ProcessBuilder("python3", "remove_status.py");
            
            // Adding name and cpf as arguments if they exist, might be useful for the script
            if (event.getUserInfo() != null) {
                if (event.getUserInfo().getName() != null) {
                    processBuilder.command().add("--name");
                    processBuilder.command().add(event.getUserInfo().getName());
                }
                if (event.getUserInfo().getCpf() != null) {
                    processBuilder.command().add("--cpf");
                    processBuilder.command().add(event.getUserInfo().getCpf());
                }
            }
            
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            log.error("Error calling remove_status.py", e);
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
