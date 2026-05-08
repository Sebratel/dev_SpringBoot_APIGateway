package br.com.sebratel.bff.controller.scripts;

import br.com.sebratel.bff.dto.ApiResponse;
import br.com.sebratel.bff.dto.InactivateAccountDTO;
import br.com.sebratel.bff.service.InactivateAccountProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/inactivate-account")
@RequiredArgsConstructor
public class InactivateAccountController {

    private final InactivateAccountProducer producer;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> inactivateAccount(@RequestBody InactivateAccountDTO request) {
        log.info("Request to inactivate account: {}", request.getUserInfo());
        try {
            producer.sendInactivationEvent(request);
            log.info("User inactivated");
            ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .success(true)
                    .message("Inactivation process successfully queued.")
                    .build();
            return ResponseEntity.accepted().body(response);
        } catch (Exception e) {
            log.error("Error inactivating account: {}", e.getMessage());
            ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .success(false)
                    .message("Error: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
