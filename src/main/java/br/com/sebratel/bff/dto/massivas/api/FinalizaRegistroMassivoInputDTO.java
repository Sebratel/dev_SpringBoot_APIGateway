package br.com.sebratel.bff.dto.massivas.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FinalizaRegistroMassivoInputDTO{
    String assignmentId;
    String incidentStatusId;
    String description;
    String progress;
    String priority;
    String notificationTarget;
    String privateReport;
}
