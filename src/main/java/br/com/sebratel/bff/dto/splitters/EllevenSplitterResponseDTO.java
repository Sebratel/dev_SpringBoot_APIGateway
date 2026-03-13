package br.com.sebratel.bff.dto.splitters;

import java.util.List;

public record EllevenSplitterResponseDTO(
        boolean success,
        Object messages,
        List<NetworkComponentDTO> response,
        String dataResponseType,
        Object elapsedTime
) {}