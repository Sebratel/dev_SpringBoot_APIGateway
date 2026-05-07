package br.com.sebratel.bff.dto.massivas.api;

import br.com.sebratel.bff.model.entity.AffectedUsersEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AberturaRegistroMassivoInputDTO {
    @NotNull
    private Integer incidentStatusId;
    @NotNull
    private Long personId;
    @NotNull
    private Integer incidentTypeId;
    @NotNull
    private Integer catalogServiceId;
    @NotNull
    private Integer serviceLevelAgreementId;
    @NotNull
    private Integer matrixType;
    @NotNull
    private String teamCode;
    @NotBlank
    private String solicitationServiceCategory1;
    private String solicitationServiceCategory2;
    private String solicitationServiceCategory3;
    private String solicitationServiceCategory4;
    private String solicitationServiceCategory5;
    private String authenticationAccessPointCode;
    @NotNull
    private AberturaRegistroMassivoAssignmentDTO assignment;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private int affectedUsersQuantity;
    private List<AffectedUsersEntity> affectedUsers = new java.util.ArrayList<>();
}
