package br.com.sebratel.bff.repository.erp;

import br.com.sebratel.bff.model.ErpContract;
import br.com.sebratel.bff.repository.erp.projections.ContratoBloqueadoProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContratoBloqueadoRepository extends JpaRepository<ErpContract, Long> {

    @Query(value = """
        WITH PERSONALIZADO AS (    
            SELECT 
                p.name AS cliente,
                c.contract_number AS contrato,
                ac."user" AS usuario,
                ac2.title AS concentrador,
                aap.title AS pontoAcesso,
                c.v_status AS statusContrato,
                c.v_stage AS estagioContrato,
                as2.title AS site,
                aal.title AS statusConexao,
                as3.title AS splitter,
                ac.city AS cidade,
                cbd.blocked_day AS diaBloqueio,
                cbd.number_of_days AS diasBloqueados,
                ROW_NUMBER() OVER (PARTITION BY ac.user ORDER BY cbd.blocked_day DESC) AS PERSONA1
            FROM erp.authentication_contracts ac
            LEFT JOIN erp.authentication_concentrators ac2 ON ac2.id = ac.authentication_concentrator_id
            LEFT JOIN erp.authentication_access_points aap ON aap.id = ac.authentication_access_point_id
            LEFT JOIN erp.contracts c ON c.id = ac.contract_id
            LEFT JOIN erp.authentication_sites as2 ON as2.id = ac2.authentication_site_id
            LEFT JOIN erp.authentication_address_lists aal ON aal.id = ac.authentication_address_list_id
            LEFT JOIN erp.authentication_splitter_ports asp ON asp.authentication_contract_id = ac.id
            LEFT JOIN erp.authentication_splitters as3 ON as3.id = asp.authentication_splitter_id
            LEFT JOIN erp.contract_blocked_days cbd ON cbd.contract_id = ac.contract_id
            LEFT JOIN erp.people p ON p.id = c.client_id
            WHERE aal.title LIKE 'mk_bloqueio'
        )
        SELECT DISTINCT 
            cliente, contrato, usuario, concentrador, pontoAcesso,
            statusContrato, estagioContrato, site, statusConexao,
            splitter, cidade, diaBloqueio, diasBloqueados
        FROM PERSONALIZADO
        WHERE PERSONA1 = 1
        """, nativeQuery = true)
    List<ContratoBloqueadoProjection> findContratosBloqueados();
}