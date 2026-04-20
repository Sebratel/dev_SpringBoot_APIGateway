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
public class DhoOpportunitiesDTO {
    private Integer id;
    private LocalDateTime openOpportunityDate;
    private String candidateName;
    private String positionName;
    private String teamName;
    private String departmentName;
    private String opportunityMotiveName;
    private String replacedPersonName;
    private String baseOriginName;
    private String opportunityStatusName;
    private Integer deadlineSlaDays;
    private LocalDateTime acceptDate;
    private String responsibleRecruiterName;
    private String observations;
}
