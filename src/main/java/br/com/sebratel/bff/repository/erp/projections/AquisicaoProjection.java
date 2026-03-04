package br.com.sebratel.bff.repository.erp.projections;

import java.time.LocalDate;

public interface AquisicaoProjection {
    Long getId();
    String getCodigo();
    String getProduto();
    LocalDate getData();
    Double getUnidades();
    String getRequisitadoPor();
    LocalDate getDataPrevisao();
    String getOutrosStatus();
    String getBase();
    String getStatus();
}