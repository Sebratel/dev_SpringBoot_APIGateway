package br.com.sebratel.bff.dto;

public record ConsumoDTO(
        String username,
        String cliente,
        String contrato,
        String plano,
        Double downloadTb,
        Double uploadTb,
        Double totalTb
) {}