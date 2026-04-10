package br.com.sebratel.bff.repository.afetados.impl;

import br.com.sebratel.bff.model.entity.InactivateAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InactivateAccountJPARepository extends JpaRepository<InactivateAccountEntity, Long> {
}
