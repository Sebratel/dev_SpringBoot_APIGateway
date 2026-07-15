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
    List<String> findByTxId(@Valid @NotNull String txId);

    @Query(value = """
        SELECT
                p.tx_id
        FROM
                people p
        WHERE
                p.email = :email
    """, nativeQuery = true)
    String findTxIdByEmail(@Valid @NotNull String email);

    @Query(value = """
        select p.tx_id 
            from authentication_contracts ac
                                inner join contracts c on c.id = ac.contract_id
                                inner join people p on p.id = c.client_id
                                where ac.contract_id = :contract;
    """, nativeQuery = true)
    String findTxIdByContract(@Valid @NotNull Long contract); // Alterado de String para Long
}
