package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.InactivateAccountDTO;
import br.com.sebratel.bff.service.InactivateAccountProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inactivate-account")
@RequiredArgsConstructor
public class InactivateAccountController {

    private final InactivateAccountProducer producer;

    @PostMapping
    public ResponseEntity<Void> inactivateAccount(@RequestBody InactivateAccountDTO request) {
        producer.sendInactivationEvent(request);
        return ResponseEntity.accepted().build();
    }
}
