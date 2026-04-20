package br.com.sebratel.bff.repository.afetados.dho;

import br.com.sebratel.bff.model.entity.dho.DhoTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DhoTeamRepository extends JpaRepository<DhoTeam, Integer> {
}
