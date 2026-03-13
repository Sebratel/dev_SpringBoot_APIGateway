package br.com.sebratel.bff.dto.massivas.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AberturaRegistroMassivoOutputDTO {
    private boolean success;
    private String messages;
    private AberturaRegistroMassivoResponseDTO response;
    private String dataResponseType;
    private Double elapsedTime;
}


