package br.com.sebratel.bff.repository.radius.projections;

import java.time.LocalDateTime;

public interface UltimaConexaoProjection {
    String getUsuario();
    LocalDateTime getInicio();
    LocalDateTime getAtualizado();
    LocalDateTime getPausado();
    Long getRecebendo();
    Long getEnviando();
    String getIpConexao();
}