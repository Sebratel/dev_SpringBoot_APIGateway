package br.com.sebratel.bff.dto;

import java.time.LocalDate;

public record DhoOpportunityDTO(
    Integer id,
    LocalDate dataAbertura,
    String cargo,
    String motivo,
    String nomeSubstituido,
    String time,
    String area,
    String local,
    String status,
    Integer prazo,
    LocalDate dataTerminoSla,
    LocalDate dataAceite,
    String situacaoPrazo,
    String recrutador,
    LocalDate dataAdmissao,
    String nome,
    String posicao,
    String gestor,
    String observacao
) {}
