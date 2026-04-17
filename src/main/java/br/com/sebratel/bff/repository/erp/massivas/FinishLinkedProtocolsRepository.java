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
            SELECT a.id 'ASSIGNMENT_LINKADO', ai.protocol 'PROTOCOLO_DA_MASSIVA', ai2.protocol 'PROTOLOCO_LINKADO', a.title 'TITULO_DA_SOLICITACAO'
            FROM assignment_links al
            INNER JOIN assignment_incidents ai ON ai.assignment_id = al.assignment_id
            LEFT JOIN assignment_incidents ai2 ON ai2.assignment_id = al.assignment_linked_id
            INNER JOIN assignments a ON a.id = ai2.assignment_id
            WHERE ai.protocol = :protocol
            """, nativeQuery = true)
    List<FinishLinkedProtocolsProjection> findLinkedProtocols(@Param("protocol") String protocol);

    @Query(value = "SELECT protocol FROM assignment_incidents WHERE assignment_id = :assignmentId", nativeQuery = true)
    String findProtocolByAssignmentId(@Param("assignmentId") String assignmentId);
}
