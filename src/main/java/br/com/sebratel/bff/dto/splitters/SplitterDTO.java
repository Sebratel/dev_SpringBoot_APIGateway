package br.com.sebratel.bff.dto.splitters;

public record SplitterDTO (
            Integer id,
            String title,
            String code,
            String integrationCode,
            Integer port,
            Integer authenticationContractId,
            OltDTO olt
)
{}
