package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.ApiResponse;
import br.com.sebratel.bff.dto.massivas.ImpactedUsersDTO;
import br.com.sebratel.bff.model.entity.UsuarioAfetado;
import br.com.sebratel.bff.service.UsuarioAfetadoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/afetados")
public class UsuarioAfetadoController {

    private final UsuarioAfetadoService usuarioAfetadoService;

    @Autowired
    public UsuarioAfetadoController(UsuarioAfetadoService usuarioAfetadoService) {
        this.usuarioAfetadoService = usuarioAfetadoService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ImpactedUsersDTO>> createUsuarioAfetado(@RequestBody List<UsuarioAfetado> usuarioAfetado) {
        log.info("Iniciando processo de criação de usuário afetado para o protocolo");
        try {
            ImpactedUsersDTO savedUsuario = usuarioAfetadoService.createImpactedUsersDTO(usuarioAfetado);
            log.info("Usuários afetados para o protocolo criado com sucesso.");

            ApiResponse<ImpactedUsersDTO> response = ApiResponse.<ImpactedUsersDTO>builder()
                    .success(true)
                    .message("Usuário afetado criado com sucesso.")
                    .data(savedUsuario)
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Erro ao criar usuários afetados para o protocolo \n{}", e.getMessage());
            ApiResponse<ImpactedUsersDTO> response = ApiResponse.<ImpactedUsersDTO>builder()
                    .success(false)
                    .message("Erro ao criar usuário afetado: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/pppoe/{pppoe}")
    public ResponseEntity<ApiResponse<ImpactedUsersDTO>> getUsuariosAfetadosByPppoe(@PathVariable String pppoe) {
        log.info("Iniciando busca de usuários afetados pelo PPPoE: {}", pppoe);
        try {
            ImpactedUsersDTO usuariosAfetados = usuarioAfetadoService.getUsuariosAfetadosByPppoe(pppoe);

            if (usuariosAfetados.getImpactedUsers().isEmpty()) {
                log.warn("Nenhum usuário afetado encontrado para o PPPoE: {}", pppoe);
            } else {
                log.info("Busca por PPPoE {} concluída. Encontrados {} usuários.", pppoe, usuariosAfetados.getImpactedUsers().size());
            }

            ApiResponse<ImpactedUsersDTO> response = ApiResponse.<ImpactedUsersDTO>builder()
                    .success(true)
                    .message("Busca por PPPoE realizada com sucesso.")
                    .data(usuariosAfetados)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erro ao buscar usuários por PPPoE {}: {}", pppoe, e.getMessage(), e);
            ApiResponse<ImpactedUsersDTO> response = ApiResponse.<ImpactedUsersDTO>builder()
                    .success(false)
                    .message("Erro ao buscar usuários por PPPoE: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/protocol/{protocol}")
    public ResponseEntity<ApiResponse<ImpactedUsersDTO>> getUsuariosAfetadosByProtocol(@PathVariable Long protocol) {
        log.info("Iniciando busca de usuários afetados pelo protocolo: {}", protocol);
        try {
            ImpactedUsersDTO usuariosAfetados = usuarioAfetadoService.getUsuariosByProtocol(protocol);

            if (usuariosAfetados.getImpactedUsers().isEmpty()) {
                log.warn("Nenhum usuário afetado encontrado para o protocolo: {}", protocol);
                ApiResponse<ImpactedUsersDTO> response = ApiResponse.<ImpactedUsersDTO>builder()
                        .success(false)
                        .message("Nenhum usuário afetado encontrado para o protocolo: " + protocol)
                        .data(usuariosAfetados)
                        .build();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            log.info("Busca por protocolo {} concluída. Encontrados {} usuários.", protocol, usuariosAfetados.getImpactedUsers().size());
            ApiResponse<ImpactedUsersDTO> response = ApiResponse.<ImpactedUsersDTO>builder()
                    .success(true)
                    .message("Usuários afetados encontrados com sucesso.")
                    .data(usuariosAfetados)
                    .build();
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao buscar usuários por protocolo {}: {}", protocol, e.getMessage(), e);
            ApiResponse<ImpactedUsersDTO> response = ApiResponse.<ImpactedUsersDTO>builder()
                    .success(false)
                    .message("Erro ao buscar usuários por protocolo: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/protocol/{protocol}")
    public ResponseEntity<ApiResponse<String>> removeUsersByProtocol(@PathVariable Long protocol) {
        log.info("Iniciando remoção de usuários afetados pelo protocolo: {}", protocol);
        try {
            usuarioAfetadoService.removeUsersByProtocol(protocol);

            log.info("Usuários do protocolo {} removidos com sucesso.", protocol);

            ApiResponse<String> response = ApiResponse.<String>builder()
                    .success(true)
                    .message("Usuários do protocolo " + protocol + " removidos com sucesso.")
                    .build();
            return ResponseEntity.ok(response); // 200 OK é mais comum para delete, mas 204 No Content também é uma opção.

        } catch (Exception e) {
            log.error("Erro ao remover usuários do protocolo {}: {}", protocol, e.getMessage(), e);
            ApiResponse<String> response = ApiResponse.<String>builder()
                    .success(false)
                    .message("Erro ao remover usuários do protocolo: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
