package br.com.sebratel.bff.dto.massivas;

import java.time.LocalDateTime;

public record PrevisaoMassivaOutputDTO(
        Long id,
        String contrato,
        String protocolo,
        LocalDateTime criacao,
        LocalDateTime previsaoFinalizacao,
        LocalDateTime finalizado,
        String status,
        String tituloIncidente,
        String descricao) {
}
