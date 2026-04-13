package br.com.sebratel.bff.repository.afetados;

import br.com.sebratel.bff.model.entity.DhoOpportunityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DhoOpportunityRepository extends JpaRepository<DhoOpportunityEntity, Integer> {
}
