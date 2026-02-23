package br.com.sebratel.bff.repository.erp.projections;

import java.time.LocalDate;

public interface ContratoBloqueadoProjection {
    String getCliente();
    String getContrato();
    String getUsuario();
    String getConcentrador();
    String getPontoAcesso();
    String getStatusContrato();
    String getEstagioContrato();
    String getSite();
    String getStatusConexao();
    String getSplitter();
    String getCidade();
    LocalDate getDiaBloqueio();
    Integer getDiasBloqueados();
}