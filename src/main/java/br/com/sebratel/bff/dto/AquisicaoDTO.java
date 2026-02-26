package br.com.sebratel.bff.dto;

import java.time.LocalDate;

public record AquisicaoDTO(
        Long id,
        String codigo,
        String produto,
        LocalDate data,
        String requisitadoPor,
        LocalDate dataPrevisao,
        Double unidades,
        String base,
        String status,
        String outroStatus
) {}