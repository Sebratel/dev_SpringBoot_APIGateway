package br.com.sebratel.bff.controller.scripts;

import br.com.sebratel.bff.dto.FirstAuthenticationDTO;
import br.com.sebratel.bff.service.FirstAuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/radius")
public class FirstAuthenticationController {

    private final FirstAuthenticationService service;

    public FirstAuthenticationController(FirstAuthenticationService service) {
        this.service = service;
    }

    @GetMapping("/first-authentications")
    public ResponseEntity<List<FirstAuthenticationDTO>> getFirstAuthentications() {
        return ResponseEntity.ok(service.listarPrimeirasAutenticacoes());
    }
}