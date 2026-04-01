package br.com.sebratel.bff.dto;

public record TechnicianInventoryDTO(

        String codigo,
        String descricao,
        String tecnico,
        Long possui,
        Long id
) {}
