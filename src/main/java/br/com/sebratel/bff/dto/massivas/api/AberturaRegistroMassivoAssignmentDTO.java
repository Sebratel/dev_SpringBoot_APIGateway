package br.com.sebratel.bff.dto.massivas.api;

import lombok.Data;

@Data
class AberturaRegistroMassivoAssignmentDTO {
    private String title;
    private String description;
    private String finalDate;
    private Integer companyPlaceId;
}