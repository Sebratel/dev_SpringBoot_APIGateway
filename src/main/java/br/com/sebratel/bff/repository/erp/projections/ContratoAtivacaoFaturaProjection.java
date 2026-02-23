package br.com.sebratel.bff.repository.erp.projections;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ContratoAtivacaoFaturaProjection {
    String getContrato();
    String getVendedor();
    String getNome();
    LocalDateTime getDataAtivacao();
    LocalDate getVencimento();
}