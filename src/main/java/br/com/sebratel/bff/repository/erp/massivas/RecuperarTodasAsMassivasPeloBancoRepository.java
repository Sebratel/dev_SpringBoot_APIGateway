package br.com.sebratel.bff.repository.erp.massivas;

import br.com.sebratel.bff.model.ErpContract;
import br.com.sebratel.bff.repository.erp.projections.RecuperarTodasAsMassivasProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecuperarTodasAsMassivasPeloBancoRepository extends JpaRepository<ErpContract, Long> {

    @Query(value = """
            SELECT
                a.id as ID,
                a.created as CRIACAO,
                a.final_date as SLA,
                a.conclusion_date as FINALIZADO,
                ai.protocol as PROTOCOLO,
                a.description as DESCRICAO,
                t.title as EQUIPE,
                t2.title as STATUS,
                it.title as TIPO_SOLICITACAO,
                v.name as SOLICITANTE, -- CREATED
                p2.name as RESPONSAVEL, -- REQUESTOR
                cs.title as CATALOGO,
                aap.title as PONTODEACESSO
            from assignments a
                left join assignment_incidents ai on ai.assignment_id = a.id
                left join teams t on t.id = ai.team_id
                left join incident_status t2 on t2.id = ai.incident_status_id
                left join incident_types it on it.id = ai.incident_type_id
                left join v_users v on v.id = a.created_by
                left join people p2 on p2.id = a.requestor_id
                left join catalog_services cs on cs.id = ai.catalog_service_id
                left join authentication_access_points aap on aap.id = ai.authentication_access_point_id
            where it.id in (1176, 302, 1257, 1265)
                and t2.title not in ('Encerrado', 'Cancelado')
            order by id desc
            """, nativeQuery = true)
    List<RecuperarTodasAsMassivasProjection> findActiveAssignments();
}