package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.ApiResponse;
import br.com.sebratel.bff.dto.DhoUserDTO;
import br.com.sebratel.bff.service.DhoUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/dho-users")
@RequiredArgsConstructor
@Tag(name = "DHO Users", description = "Endpoints for managing DHO users")
public class DhoUserController {

    private final DhoUserService service;

    @GetMapping
    @Operation(summary = "List all DHO users")
    public ResponseEntity<ApiResponse<List<DhoUserDTO>>> findAll() {
        log.info("Received request to list DHO users");
        try {
            List<DhoUserDTO> users = service.findAll();
            ApiResponse<List<DhoUserDTO>> response = ApiResponse.<List<DhoUserDTO>>builder()
                    .success(true)
                    .message("Users successfully found.")
                    .data(users)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error searching for DHO users: {}", e.getMessage(), e);
            ApiResponse<List<DhoUserDTO>> response = ApiResponse.<List<DhoUserDTO>>builder()
                    .success(false)
                    .message("Error searching for users: " + e.getMessage())
                    .build();
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
