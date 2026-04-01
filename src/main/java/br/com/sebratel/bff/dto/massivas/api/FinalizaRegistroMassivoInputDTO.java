package br.com.sebratel.bff.dto.massivas.api;

import lombok.Data;

@Data
public class FinalizaRegistroMassivoInputDTO{
    String assignmentId;
    String incidentStatusId;
    String description;
    String progress;
    String priority;
    String notificationTarget;
    String privateReport;
}
