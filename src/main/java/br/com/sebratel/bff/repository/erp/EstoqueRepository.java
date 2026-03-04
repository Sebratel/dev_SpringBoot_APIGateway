package br.com.sebratel.bff.repository.erp;

import br.com.sebratel.bff.model.ErpContract;
import br.com.sebratel.bff.repository.erp.projections.EstoqueProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EstoqueRepository extends JpaRepository<ErpContract, Long> {

    @Query(value = """
        SELECT MAX(ppm.id) AS id, 
               sp.code AS codigo, 
               sp.title AS descricao, 
               p.name AS tecnico,
               SUM(CASE WHEN ppm.signal = 1 THEN ppm.units ELSE 0 END) -
               SUM(CASE WHEN ppm.signal = 2 THEN ppm.units ELSE 0 END) AS possui
        FROM erp.person_product_movimentations ppm
        LEFT JOIN erp.service_products sp ON sp.id = ppm.service_product_id
        LEFT JOIN erp.people p ON p.id = ppm.person_id
        WHERE UPPER(TRIM(p.name)) = UPPER(TRIM(:nomeTecnico))
        GROUP BY sp.code, sp.title, p.name
        HAVING SUM(CASE WHEN ppm.signal = 1 THEN ppm.units ELSE 0 END) -
               SUM(CASE WHEN ppm.signal = 2 THEN ppm.units ELSE 0 END) > 0
        """, nativeQuery = true)
    List<EstoqueProjection> executeNativeQuery(@Param("nomeTecnico") String nomeTecnico);

}