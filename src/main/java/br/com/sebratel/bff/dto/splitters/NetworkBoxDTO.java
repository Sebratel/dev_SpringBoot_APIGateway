package br.com.sebratel.bff.dto.splitters;

import br.com.sebratel.bff.dto.splitters.ValueTextDTO;

public record NetworkBoxDTO(
        Long id,
        String code,
        String title,
        boolean active,
        ValueTextDTO status,
        ValueTextDTO type,
        String integrationCode
) {}