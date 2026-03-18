package br.com.sebratel.bff.repository.erp;

import br.com.sebratel.bff.model.entity.PersonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
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
}
