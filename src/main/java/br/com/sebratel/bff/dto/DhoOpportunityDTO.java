package br.com.sebratel.bff.dto;

import java.time.LocalDateTime;

public record DhoOpportunityDTO(
    Long id,
    Integer registration,
    String email,
    LocalDateTime admissionDate,
    String status,
    String operationalBase,
    String team,
    String position,
    String supervisorName,
    String managerName,
    String coordinatorName
) {}
