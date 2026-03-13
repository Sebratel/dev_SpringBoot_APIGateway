package br.com.sebratel.bff.dto.splitters;

import java.util.List;

public record EllevenOltsResponseDTO(
        boolean success,
        Object messages,
        List<OltResponseDTO> response,
        String dataResponseType,
        Object elapsedTime
) {}