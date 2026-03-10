package br.com.sebratel.bff.dto.massivas;

import lombok.Data;

@Data
public class EllevenApiResponseDTO {
    private boolean success;
    private Object messages;
    private ResponseDataDTO response;
    private String dataResponseType;
    private Object elapsedTime;
}
