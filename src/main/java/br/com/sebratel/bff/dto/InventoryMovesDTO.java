package br.com.sebratel.bff.dto;
public record InventoryMovesDTO(
        String codigos,
        Long id,
        String descricao,
        String baseDeOrigem,
        Double estoqueAtual,
        Integer minimo
) {}