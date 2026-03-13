package br.com.sebratel.bff.dto.massivas.api;

public record FinalizaRegistroMassivoInputDTO (
    String assignmentId,
    String incidentStatusId,
    String description,
    String progress,
    String priority,
    String notificationTarget,
    String privateReport
){}
