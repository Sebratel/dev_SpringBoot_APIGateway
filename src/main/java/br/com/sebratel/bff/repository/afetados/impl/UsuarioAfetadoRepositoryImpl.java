package br.com.sebratel.bff.repository.afetados.impl;

import br.com.sebratel.bff.model.entity.UsuarioAfetadoEntity;
import br.com.sebratel.bff.repository.afetados.UsuarioAfetadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class UsuarioAfetadoRepositoryImpl implements UsuarioAfetadoRepository {

    private final UsuarioAfetadoJPARepository jpaRepository;

    @Autowired
    public UsuarioAfetadoRepositoryImpl(UsuarioAfetadoJPARepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<UsuarioAfetadoEntity> saveAll(List<UsuarioAfetadoEntity> usuarioAfetadoEntity) {
        return jpaRepository.saveAll(usuarioAfetadoEntity);
    }

    @Override
    public Optional<UsuarioAfetadoEntity> findByPppoe(String pppoe) {
        return jpaRepository.findByPppoe(pppoe);
    }

    @Override
    public List<UsuarioAfetadoEntity> findByProtocol(Long protocol) {
        return jpaRepository.findByProtocol(protocol);
    }

    @Override
    public Integer deleteByProtocol(Long protocol) {
        return jpaRepository.deleteByProtocol(protocol);
    }

    @Override
    public List<UsuarioAfetadoEntity> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public Integer updateUsersByProtocol(Long protocol, LocalDateTime finishDate) {
        return jpaRepository.updateUsersByProtocol(protocol, finishDate);
    }

    @Override
    public Optional<UsuarioAfetadoEntity> findByContractId(Long contractId) {
        return jpaRepository.findByContractId(contractId);
    }
}
