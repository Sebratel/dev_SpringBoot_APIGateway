package br.com.sebratel.bff.repository.afetados;

import br.com.sebratel.bff.model.entity.DhoSettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DhoSettingRepository extends JpaRepository<DhoSettingEntity, String> {
}
