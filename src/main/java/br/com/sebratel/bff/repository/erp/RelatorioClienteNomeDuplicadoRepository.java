package br.com.sebratel.bff.repository.erp;

import br.com.sebratel.bff.model.ErpContract;
import br.com.sebratel.bff.repository.erp.projections.RelatorioClienteNomeDuplicadoProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RelatorioClienteNomeDuplicadoRepository extends JpaRepository<ErpContract, Long> {

    @Query(value = """
        WITH AuthenticatedUsers AS (
            SELECT
                ac.user AS authenticated_user,
                ac.contract_id AS auth_contract_id,
                c.description AS auth_contract_description,
                p.name AS client_name
            FROM erp.authentication_contracts ac
            INNER JOIN erp.contracts c ON ac.contract_id = c.id
            LEFT JOIN erp.people p ON p.id = c.client_id 
        ),
        ConnectionEvents AS (
            SELECT DISTINCT
                ce.contract_id AS event_contract_id,
                ce.description AS event_description,
                p.name AS client_name
            FROM erp.contract_events ce
            LEFT JOIN erp.contracts c ON c.id = ce.contract_id
            LEFT JOIN erp.people p ON p.id = c.client_id 
            WHERE ce.description LIKE 'Conexão Excluída%'
        )
        SELECT DISTINCT 
            au.authenticated_user AS authenticatedUser,
            au.auth_contract_description AS authContractDescription,
            ce.event_description AS eventDescription
        FROM AuthenticatedUsers au
        INNER JOIN ConnectionEvents ce ON ce.event_description = 'Conexão Excluída. Username: ' || au.authenticated_user
        AND au.client_name <> ce.client_name
        """, nativeQuery = true)
    List<RelatorioClienteNomeDuplicadoProjection> findClientesNomesDuplicados();
}