package br.com.sebratel.bff.dto.splitters;

import java.util.List;

public record EllevenSplitterResponseDTO<T>(
        boolean success,
        Object messages,
        T response,
        String dataResponseType,
        Object elapsedTime
) {}