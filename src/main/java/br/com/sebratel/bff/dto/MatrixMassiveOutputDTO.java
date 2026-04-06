package br.com.sebratel.bff.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatrixMassiveOutputDTO {

    @JsonProperty("authentication_problems")
    private Long authenticationProblems;

    @JsonProperty("resolution_time")
    private Integer resolutionTime;

    @JsonProperty("resolution_time_hour")
    private String resolutionTimeHour;

    @JsonProperty("status")
    private String status;

}