package br.com.sebratel.bff.dto;

public record DhoSettingDTO(
    String cargo,
    String time,
    String motivo,
    String area,
    String local,
    String statusVaga,
    String recrutador,
    String tipoDeDemissao,
    String motivacao,
    String situacao,
    String escolaridade,
    String etapa,
    String fonte,
    String gestor
) {}
