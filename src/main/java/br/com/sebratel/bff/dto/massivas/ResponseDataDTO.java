package br.com.sebratel.bff.dto.massivas;

import lombok.Data;

import java.util.List;

@Data
public class ResponseDataDTO {
    private List<IncidentDTO> data;
    private int totalRecords;
    private int page;
    private int pageSize;
    private Object totalizers;
    private int totalPages;
}
