package br.com.sebratel.bff.repository.erp.massivas;

import br.com.sebratel.bff.model.ErpContract;
import br.com.sebratel.bff.repository.erp.projections.FinishLinkedProtocolsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinishLinkedProtocolsRepository extends JpaRepository<ErpContract, Long> {

    @Query(value = """
            select
                a.id "ASSIGNMENT_LINKADO",
                ai.protocol "PROTOCOLO_DA_MASSIVA",
                ai2.protocol "PROTOLOCO_LINKADO",
                a.title "TITULO_DA_SOLICITACAO"
                from assignment_links al
                inner join assignment_incidents ai on ai.assignment_id = al.assignment_id
                left join assignment_incidents ai2 on ai2.assignment_id = al.assignment_linked_id
                inner join assignments a on a.id = ai.assignment_id
                where a.id= :assignment
            """, nativeQuery = true)
    List<FinishLinkedProtocolsProjection> findLinkedProtocols(@Param("assignment") String assignment);
}
