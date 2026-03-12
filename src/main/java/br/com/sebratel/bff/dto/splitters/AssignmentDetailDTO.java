package br.com.sebratel.bff.dto.splitters;

public record AssignmentDetailDTO(
        Long assignmentId,
        String title,
        Long protocol,
        String status,
        String team,
        String sectorArea,
        String beginningData,
        String finalData
) {}