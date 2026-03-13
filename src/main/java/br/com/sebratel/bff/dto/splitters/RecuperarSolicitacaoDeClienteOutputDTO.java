package br.com.sebratel.bff.dto.splitters;

import java.util.List;

public record RecuperarSolicitacaoDeClienteOutputDTO(
        boolean success,
        List<String> messages,
        AssignmentResponseDataDTO response,
        String dataResponseType,
        Object elapsedTime
) {}
