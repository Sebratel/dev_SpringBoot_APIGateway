package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.ApiResponse;
import br.com.sebratel.bff.dto.AuthenticationSitesInputDTO;
import br.com.sebratel.bff.dto.AuthenticationSitesOutputDTO;
import br.com.sebratel.bff.service.AuthenticationSitesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("api/v1/sites")
@Slf4j
@RestController
public class AuthenticationSitesController {

    private final AuthenticationSitesService authenticationSitesService;

    @Autowired
    public AuthenticationSitesController(AuthenticationSitesService authenticationSitesService) {
        this.authenticationSitesService = authenticationSitesService;
    }

    @GetMapping
    public ApiResponse<List<AuthenticationSitesOutputDTO>> getSites(@RequestParam String title) {
        return ApiResponse.<List<AuthenticationSitesOutputDTO>>builder()
                .success(true)
                .data(authenticationSitesService.execute(title))
                .message("Sites retrivied sucessfully")
                .build();
    }

    /** Busca por título (contém) para o seletor de site — usado na abertura de protocolo de backbone. */
    @GetMapping("/search")
    public ApiResponse<List<AuthenticationSitesOutputDTO>> searchSites(@RequestParam String q) {
        return ApiResponse.<List<AuthenticationSitesOutputDTO>>builder()
                .success(true)
                .data(authenticationSitesService.search(q))
                .message("Sites retrieved successfully")
                .build();
    }
}
