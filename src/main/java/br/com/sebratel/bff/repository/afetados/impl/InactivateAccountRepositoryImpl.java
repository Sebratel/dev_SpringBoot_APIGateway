package br.com.sebratel.bff.repository.afetados.impl;

import br.com.sebratel.bff.model.entity.InactivateAccountEntity;
import br.com.sebratel.bff.repository.afetados.InactivateAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class InactivateAccountRepositoryImpl implements InactivateAccountRepository {

    private final InactivateAccountJPARepository jpaRepository;

    @Autowired
    public InactivateAccountRepositoryImpl(InactivateAccountJPARepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public InactivateAccountEntity save(InactivateAccountEntity entity) {
        return jpaRepository.save(entity);
    }
}
