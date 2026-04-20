package br.com.sebratel.bff.model.entity.dho;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "opportunities")
@NoArgsConstructor
@AllArgsConstructor
public class DhoOpportunities {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "open_opportunity_date")
    private LocalDateTime openOpportunityDate;

    @ManyToOne
    @JoinColumn(name = "people_id")
    private DhoPeople candidate;

    @ManyToOne
    @JoinColumn(name = "position_id")
    private DhoPosition position;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private DhoTeam team;

    @ManyToOne
    @JoinColumn(name = "departament_id")
    private DhoDepartament departament;

    @ManyToOne
    @JoinColumn(name = "opportunity_motive_id")
    private DhoOpportunityMotive opportunityMotive;

    @ManyToOne
    @JoinColumn(name = "replaced_person_id")
    private DhoPeople replacedPerson;

    @ManyToOne
    @JoinColumn(name = "base_origin_id")
    private DhoBaseOrigin baseOrigin;

    @ManyToOne
    @JoinColumn(name = "opportunity_status_id")
    private DhoOpportunityStatus opportunityStatus;

    @Column(name = "deadline_sla_days")
    private Integer deadlineSlaDays;

    @Column(name = "accept_date")
    private LocalDateTime acceptDate;

    @ManyToOne
    @JoinColumn(name = "responsible_recruiter_id")
    private DhoPeople responsibleRecruiter;

    private String observations;
}
