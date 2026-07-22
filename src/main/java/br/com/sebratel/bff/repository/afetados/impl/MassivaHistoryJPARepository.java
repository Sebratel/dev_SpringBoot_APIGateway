package br.com.sebratel.bff.repository.afetados.impl;

import br.com.sebratel.bff.model.entity.MassivaHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MassivaHistoryJPARepository extends JpaRepository<MassivaHistoryEntity, Long> {
}
