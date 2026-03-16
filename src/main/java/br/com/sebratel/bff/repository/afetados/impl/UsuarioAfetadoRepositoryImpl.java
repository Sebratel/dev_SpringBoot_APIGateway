package br.com.sebratel.bff.repository.afetados.impl;

import br.com.sebratel.bff.model.entity.UsuarioAfetado;
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
    public List<UsuarioAfetado> saveAll(List<UsuarioAfetado> usuarioAfetado) {
        return jpaRepository.saveAll(usuarioAfetado);
    }

    @Override
    public Optional<UsuarioAfetado> findByPppoe(String pppoe) {
        return jpaRepository.findByPppoe(pppoe);
    }

    @Override
    public List<UsuarioAfetado> findByProtocol(Long protocol) {
        return jpaRepository.findByProtocol(protocol);
    }

    @Override
    public Integer deleteByProtocol(Long protocol) {
        return jpaRepository.deleteByProtocol(protocol);
    }

    @Override
    public List<UsuarioAfetado> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public Integer updateUsersByProtocol(Long protocol, LocalDateTime finishDate) {
        return jpaRepository.updateUsersByProtocol(protocol, finishDate);
    }

    @Override
    public Optional<UsuarioAfetado> findByContractId(Long contractId) {
        return jpaRepository.findByContractId(contractId);
    }
}
