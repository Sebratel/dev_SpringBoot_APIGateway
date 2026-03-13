package br.com.sebratel.bff.dto.massivas;

import java.time.LocalDateTime;

public record MassivasBFFOutputDTO(
        Long id,
        LocalDateTime criacao,
        LocalDateTime finalizado,
        String protocolo,
        String equipe,
        String status,
        String tituloIncidente,
        String criadoPor,
        String responsavel) {
}
