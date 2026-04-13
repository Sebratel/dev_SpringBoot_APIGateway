package br.com.sebratel.bff.repository.afetados;

import br.com.sebratel.bff.model.entity.DhoUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DhoUserRepository extends JpaRepository<DhoUserEntity, Integer> {
    Optional<DhoUserEntity> findByEmail(String email);
}
