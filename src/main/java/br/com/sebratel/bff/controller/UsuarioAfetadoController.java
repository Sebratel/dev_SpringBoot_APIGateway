package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.ApiResponse;
import br.com.sebratel.bff.dto.massivas.ImpactedUsersOutputDTO;
import br.com.sebratel.bff.model.entity.UsuarioAfetadoEntity;
import br.com.sebratel.bff.service.UsuarioAfetadoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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

    @GetMapping
    public ResponseEntity<ApiResponse<ImpactedUsersOutputDTO>> getAllImpactedUsers() {
        log.info("Iniciando busca de usuárioss afetados");
        try {
            ImpactedUsersOutputDTO usuariosAfetados = usuarioAfetadoService.getAll();

            if (usuariosAfetados.getImpactedUsers().isEmpty()) {
                log.warn("Nenhum usuário afetado encontrado.");
                ApiResponse<ImpactedUsersOutputDTO> response = ApiResponse.<ImpactedUsersOutputDTO>builder()
                        .success(false)
                        .message("Nenhum usuário afetado encontrado")
                        .data(usuariosAfetados)
                        .build();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            log.info("Busca concluída. Encontrados {} usuários.", usuariosAfetados.getImpactedUsers().size());
            ApiResponse<ImpactedUsersOutputDTO> response = ApiResponse.<ImpactedUsersOutputDTO>builder()
                    .success(true)
                    .message("Usuários afetados encontrados com sucesso.")
                    .data(usuariosAfetados)
                    .build();
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao buscar usuários: {}", e.getMessage(), e);
            ApiResponse<ImpactedUsersOutputDTO> response = ApiResponse.<ImpactedUsersOutputDTO>builder()
                    .success(false)
                    .message("Erro ao buscar usuários: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ImpactedUsersOutputDTO>> createUsuarioAfetado(@RequestBody List<UsuarioAfetadoEntity> usuarioAfetadoEntity) {
        log.info("Iniciando processo de criação de usuário afetado para o protocolo");
        try {
            ImpactedUsersOutputDTO savedUsuario = usuarioAfetadoService.createImpactedUsersDTO(usuarioAfetadoEntity);
            log.info("Usuários afetados para o protocolo criado com sucesso.");

            ApiResponse<ImpactedUsersOutputDTO> response = ApiResponse.<ImpactedUsersOutputDTO>builder()
                    .success(true)
                    .message("Usuário afetado criado com sucesso.")
                    .data(savedUsuario)
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Erro ao criar usuários afetados para o protocolo \n{}", e.getMessage());
            ApiResponse<ImpactedUsersOutputDTO> response = ApiResponse.<ImpactedUsersOutputDTO>builder()
                    .success(false)
                    .message("Erro ao criar usuário afetado: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/pppoe/{pppoe}")
    public ResponseEntity<ApiResponse<ImpactedUsersOutputDTO>> getUsuariosAfetadosByPppoe(@PathVariable String pppoe) {
        log.info("Iniciando busca de usuários afetados pelo PPPoE: {}", pppoe);
        try {
            ImpactedUsersOutputDTO usuariosAfetados = usuarioAfetadoService.getUsuariosAfetadosByPppoe(pppoe);

            if (usuariosAfetados.getImpactedUsers().isEmpty()) {
                log.warn("Nenhum usuário afetado encontrado para o PPPoE: {}", pppoe);
            } else {
                log.info("Busca por PPPoE {} concluída. Encontrados {} usuários.", pppoe, usuariosAfetados.getImpactedUsers().size());
            }

            ApiResponse<ImpactedUsersOutputDTO> response = ApiResponse.<ImpactedUsersOutputDTO>builder()
                    .success(true)
                    .message("Busca por PPPoE realizada com sucesso.")
                    .data(usuariosAfetados)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erro ao buscar usuários por PPPoE {}: {}", pppoe, e.getMessage(), e);
            ApiResponse<ImpactedUsersOutputDTO> response = ApiResponse.<ImpactedUsersOutputDTO>builder()
                    .success(false)
                    .message("Erro ao buscar usuários por PPPoE: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/contract/{contractId}")
    public ResponseEntity<ApiResponse<ImpactedUsersOutputDTO>> getUsuariosAfetadosByContractId(@PathVariable Long contractId) {
        log.info("Iniciando busca de usuários afetados pelo contractId: {}", contractId);
        try {
            ImpactedUsersOutputDTO usuariosAfetados = usuarioAfetadoService.getUsuariosAfetadosByContractId(contractId);

            if (usuariosAfetados.getImpactedUsers().isEmpty()) {
                log.warn("Nenhum usuário afetado encontrado para o contractId: {}", contractId);
            } else {
                log.info("Busca por contractId {} concluída. Encontrados {} usuários.", contractId, usuariosAfetados.getImpactedUsers().size());
            }

            ApiResponse<ImpactedUsersOutputDTO> response = ApiResponse.<ImpactedUsersOutputDTO>builder()
                    .success(true)
                    .message("Busca por PPPoE realizada com sucesso.")
                    .data(usuariosAfetados)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erro ao buscar usuários por PPPoE {}: {}", contractId, e.getMessage(), e);
            ApiResponse<ImpactedUsersOutputDTO> response = ApiResponse.<ImpactedUsersOutputDTO>builder()
                    .success(false)
                    .message("Erro ao buscar usuários por PPPoE: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @GetMapping("/protocol/{protocol}")
    public ResponseEntity<ApiResponse<ImpactedUsersOutputDTO>> getUsuariosAfetadosByProtocol(@PathVariable Long protocol) {
        log.info("Iniciando busca de usuários afetados pelo protocolo: {}", protocol);
        try {
            ImpactedUsersOutputDTO usuariosAfetados = usuarioAfetadoService.getUsuariosByProtocol(protocol);

            if (usuariosAfetados.getImpactedUsers().isEmpty()) {
                log.warn("Nenhum usuário afetado encontrado para o protocolo: {}", protocol);
                ApiResponse<ImpactedUsersOutputDTO> response = ApiResponse.<ImpactedUsersOutputDTO>builder()
                        .success(false)
                        .message("Nenhum usuário afetado encontrado para o protocolo: " + protocol)
                        .data(usuariosAfetados)
                        .build();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            log.info("Busca por protocolo {} concluída. Encontrados {} usuários.", protocol, usuariosAfetados.getImpactedUsers().size());
            ApiResponse<ImpactedUsersOutputDTO> response = ApiResponse.<ImpactedUsersOutputDTO>builder()
                    .success(true)
                    .message("Usuários afetados encontrados com sucesso.")
                    .data(usuariosAfetados)
                    .build();
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao buscar usuários por protocolo {}: {}", protocol, e.getMessage(), e);
            ApiResponse<ImpactedUsersOutputDTO> response = ApiResponse.<ImpactedUsersOutputDTO>builder()
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

    @PatchMapping("/protocol/{protocol}")
    public ResponseEntity<ApiResponse<ImpactedUsersOutputDTO>> alterarDataDeFinalizacaoPorProtocolo(@PathVariable Long protocol, @RequestParam LocalDateTime finishDate)
    {
        log.info("Iniciando alteração de data de finalização para o protocolo: {}", protocol);
        try {
            usuarioAfetadoService.alterarDataEstimadaParaFinalizacao(protocol, finishDate);
            log.info("Data de finalização alterada para {} nos usuarios de protocolo {}", finishDate, protocol);
            ApiResponse<ImpactedUsersOutputDTO> response = ApiResponse.<ImpactedUsersOutputDTO>builder()
                    .success(true)
                    .message("Data de finalização alterada para " + finishDate + " nos usuarios de protocolo " + protocol)
                    .data(null)
                    .build();
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao alterar data de finalização para o protocolo {}: {}", protocol, e.getMessage(), e);
            ApiResponse<ImpactedUsersOutputDTO> response = ApiResponse.<ImpactedUsersOutputDTO>builder()
                    .success(false)
                    .data(null)
                    .message("Erro ao alterar data de finalização para o protocolo: " + protocol)
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
