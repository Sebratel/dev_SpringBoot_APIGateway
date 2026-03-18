package br.com.sebratel.bff.repository.erp.splitters;

import br.com.sebratel.bff.model.ErpContract;
import br.com.sebratel.bff.repository.erp.projections.SplitterProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SplitterRepository extends JpaRepository<ErpContract, Long> {

    @Query(value = """
SELECT * from authentication_splitters where id = :splitterId
                                             """, nativeQuery = true)
    Optional<SplitterProjection> getSplitterById(Long splitterId);
}
