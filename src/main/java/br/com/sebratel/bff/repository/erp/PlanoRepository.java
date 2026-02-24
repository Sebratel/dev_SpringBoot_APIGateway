package br.com.sebratel.bff.repository.erp;

import br.com.sebratel.bff.model.ErpContract;
import br.com.sebratel.bff.repository.erp.projections.PlanoProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlanoRepository extends JpaRepository<ErpContract, Long> {

    @Query(value = """
        SELECT 
            ac.user AS username, 
            sp.title AS plano, 
            c.contract_number AS contrato, 
            p.name AS cliente 
        FROM erp.authentication_contracts ac 
        LEFT JOIN erp.service_products sp ON sp.id = ac.service_product_id 
        LEFT JOIN erp.contracts c ON c.id = ac.contract_id 
        LEFT JOIN erp.people p ON p.id = c.client_id
        """, nativeQuery = true)
    List<PlanoProjection> findTodosPlanos();

    @Query(value = """
        SELECT 
            ac.user AS username, 
            sp.title AS plano, 
            c.contract_number AS contrato, 
            p.name AS cliente 
        FROM erp.authentication_contracts ac 
        LEFT JOIN erp.service_products sp ON sp.id = ac.service_product_id 
        LEFT JOIN erp.contracts c ON c.id = ac.contract_id 
        LEFT JOIN erp.people p ON p.id = c.client_id
        WHERE ac.user IN (:usernames)
        """, nativeQuery = true)
    List<PlanoProjection> findPlanosPorUsernames(@Param("usernames") List<String> usernames);

}