package br.com.sebratel.bff.repository.afetados;

import br.com.sebratel.bff.model.entity.UsuarioAfetadoEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface UsuarioAfetadoRepository {
    List<UsuarioAfetadoEntity> saveAll(List<UsuarioAfetadoEntity> usuarioAfetadoEntity);
    Optional<UsuarioAfetadoEntity> findByPppoe(String pppoe);
    List<UsuarioAfetadoEntity> findByProtocol(Long protocol);
    Integer deleteByProtocol(Long protocol);
    List<UsuarioAfetadoEntity> findAll();
    Integer updateUsersByProtocol(Long protocol, LocalDateTime finishDate);
    Optional<UsuarioAfetadoEntity> findByContractId(Long contractId);
}
