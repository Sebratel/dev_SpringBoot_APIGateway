package br.com.sebratel.bff.repository.erp;

import br.com.sebratel.bff.model.entity.PersonEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("erpEmployeeRepository")
public interface EmployeeRepository extends JpaRepository<PersonEntity, Long> {

    @Query(value = """
            SELECT
                p.id
            FROM
                v_users vu
            INNER JOIN
                people p on vu.email = p.email
            WHERE
                vu.email = :email
            """, nativeQuery = true)
    Optional<Long> findPersonIdByEmail(String email);

    @Query(value = """
        select EXISTS(
            SELECT 
                    1
            FROM 
                    authentication_contracts ac
            JOIN 
                    contracts c ON c.id = ac.contract_id
            JOIN 
                    people p ON p.id = c.client_id
            JOIN 
                    insignias i ON i.id = p.insignia_id
            where  
                    c.id IN (:list)
            AND
                    i.title IN ('Contrato Corporativo PME', 'Contrato Corporativo')
        )
    """, nativeQuery = true)
    boolean hasB2BinInput(@Param("list") List<Long> list);

    @Query(value = """
        SELECT
                p.name
        FROM
                people p
        WHERE
                p.tx_id = :txId
    """, nativeQuery = true)
    String findByTxId(@Valid @NotNull String txId);
}
