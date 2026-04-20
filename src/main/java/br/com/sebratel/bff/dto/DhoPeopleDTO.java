package br.com.sebratel.bff.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DhoPeopleDTO {
    private Integer id;
    private String profileImage;
    private Integer registrationNumber;
    private String name;
    private String phoneNumber;
    private String cpf;
    private String rg;
    private LocalDateTime dateBirth;
    private String sex;
    private Boolean replacement;
    private LocalDateTime recruitmentData;
    private LocalDateTime admissionDate;
    private String observations;
    private String professionalReferences;
    private String collaboratorKnowledge;
    private String laborLawsuit;
    private Boolean criminalBackground;
    private String externalLink;
    private String mindsigthLink;
    private String cisLink;
    private String resignationMotivation;
    private String resignationType;
}
