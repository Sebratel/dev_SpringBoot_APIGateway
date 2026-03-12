package br.com.sebratel.bff.dto.splitters;

import java.util.List;

public record AssignmentResponseDataDTO(
        String status,
        Integer count,
        List<AssignmentDetailDTO> data
) {}