package br.com.sebratel.bff.repository.erp;

import br.com.sebratel.bff.model.entity.PersonEntity;
import br.com.sebratel.bff.repository.erp.projections.ContractProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<PersonEntity, Long> {
    Optional<PersonEntity> findByTxId(String cpf);

    @Query(value = """
        SELECT 
            ac.contract_id as contractId
        FROM people p
        INNER JOIN contracts c on p.id = c.client_id
        INNER JOIN authentication_contracts ac on ac.contract_id = c.id
        WHERE p.tx_id = :cpf
        """, nativeQuery = true)
    Optional<ContractProjection> findContractByCPF(String cpf);
}
