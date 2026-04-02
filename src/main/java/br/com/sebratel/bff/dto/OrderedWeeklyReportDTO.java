package br.com.sebratel.bff.dto;

import java.util.List;

public record OrderedWeeklyReportDTO(
        List<RelatorioFinalDTO> data
) {}