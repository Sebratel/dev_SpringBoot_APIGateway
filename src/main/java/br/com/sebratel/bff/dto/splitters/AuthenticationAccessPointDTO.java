package br.com.sebratel.bff.dto.splitters;

public record AuthenticationAccessPointDTO (

    Integer id,
    String code,
    String title,
    String integrationCodeMap,
    Integer portOlt,
    Integer slotOlt
){}
