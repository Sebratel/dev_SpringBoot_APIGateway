package br.com.sebratel.bff.model.entity.dho;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "people")
@NoArgsConstructor
@AllArgsConstructor
public class DhoPeople {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "registration_number")
    private Integer registrationNumber;

    private String name;

    @Column(name = "phone_number")
    private String phoneNumber;

    private String cpf;
    private String rg;

    @Column(name = "date_birth")
    private LocalDateTime dateBirth;

    private String sex;
    private Boolean replacement;

    @Column(name = "recruitment_data")
    private LocalDateTime recruitmentData;

    @Column(name = "adimission_date")
    private LocalDateTime admissionDate;

    private String observations;

    @Column(name = "professional_references")
    private String professionalReferences;

    @Column(name = "collaborator_knowledge")
    private String collaboratorKnowledge;

    @Column(name = "labor_lawsuit")
    private String laborLawsuit;

    @Column(name = "criminal_background")
    private Boolean criminalBackground;

    @Column(name = "external_link")
    private String externalLink;

    @Column(name = "mindsigth_link")
    private String mindsigthLink;

    @Column(name = "cis_link")
    private String cisLink;

    @ManyToOne
    @JoinColumn(name = "id_resignation_motivation")
    private DhoResignationMotivation resignationMotivation;

    @ManyToOne
    @JoinColumn(name = "id_resignation_type")
    private DhoResignationType resignationType;
}
