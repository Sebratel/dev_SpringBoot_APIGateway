package br.com.sebratel.bff.repository.afetados;

import br.com.sebratel.bff.model.entity.UsuarioAfetado;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface UsuarioAfetadoRepository {
    List<UsuarioAfetado> saveAll(List<UsuarioAfetado> usuarioAfetado);
    Optional<UsuarioAfetado> findByPppoe(String pppoe);
    List<UsuarioAfetado> findByProtocol(Long protocol);
    Integer deleteByProtocol(Long protocol);
    List<UsuarioAfetado> findAll();
    Integer updateUsersByProtocol(Long protocol, LocalDateTime finishDate);
    Optional<UsuarioAfetado> findByContractId(Long contractId);
}
