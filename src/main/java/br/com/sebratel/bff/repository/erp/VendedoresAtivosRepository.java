package br.com.sebratel.bff.repository.erp;

import br.com.sebratel.bff.model.ErpContract;
import br.com.sebratel.bff.repository.erp.projections.VendedoresProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendedoresAtivosRepository extends JpaRepository<ErpContract, Long> {

    @Query(value = """
        SELECT 
            p.name AS nomeVendedor, 
            p.email AS emailVendedor 
        FROM erp.people_crm_informations x
        LEFT JOIN people p ON x.person_id = p.id 
        LEFT JOIN v_users vu ON p.name = vu.name
        WHERE x.industry_sector_id IS NOT NULL 
          AND p.email <> ''
          AND vu.active IS TRUE
        ORDER BY p.name ASC
        """, nativeQuery = true)
    List<VendedoresProjection> findVendedoresAtivos();
}