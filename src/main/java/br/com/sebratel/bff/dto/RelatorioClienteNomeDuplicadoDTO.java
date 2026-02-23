package br.com.sebratel.bff.dto;

public record RelatorioClienteNomeDuplicadoDTO(
        String authenticatedUser,
        String authContractDescription,
        String eventDescription
) {}