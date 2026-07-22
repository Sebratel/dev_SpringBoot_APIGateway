package br.com.sebratel.bff.repository.erp.projections;

import java.time.LocalDateTime;

public interface PrevisaoMassivaPorContratoProjection {
    Long getID();

    String getCONTRATO();

    String getPROTOCOLO();

    LocalDateTime getCRIACAO();

    LocalDateTime getPREVISAO();

    LocalDateTime getFINALIZADO();

    String getSTATUS();

    String getTIPO_SOLICITACAO();

    String getDESCRICAO();
}
