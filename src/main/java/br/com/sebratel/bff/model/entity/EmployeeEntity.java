package br.com.sebratel.bff.model.entity;

import br.com.sebratel.bff.enums.JobRoleEnum;
import br.com.sebratel.bff.enums.TeamEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "db_active_collaborators")
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private Integer registration;
    @Column(name="admission_date")
    private LocalDateTime admissionDate;
    private String status;
    @Column(name="operational_base")
    private String operationalBase;
    @Enumerated(EnumType.STRING)
    private TeamEnum team;
    @Enumerated(EnumType.STRING)
    private JobRoleEnum position;
    @Column(name="supervisor_name")
    private String supervisorName;
    @Column(name="manager_name")
    private String managerName;
    @Column(name = "coordinator_name")
    private String coordinatorName;

}
