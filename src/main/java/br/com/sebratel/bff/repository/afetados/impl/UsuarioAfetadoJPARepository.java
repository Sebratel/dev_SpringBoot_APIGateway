package br.com.sebratel.bff.repository.afetados.impl;

import br.com.sebratel.bff.model.entity.AffectedUsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;


@Repository
public interface UsuarioAfetadoJPARepository extends JpaRepository<AffectedUsersEntity, Long> {
    Optional<AffectedUsersEntity> findByPppoe(String pppoe);
    // Um cliente pode ter mais de uma linha (ex.: massiva e GMUD ao mesmo tempo). Entre elas, a MASSIVA
    // vale primeiro (origin desc) e, empatando, a de previsao mais proxima.
    Optional<AffectedUsersEntity> findFirstByPppoeOrderByOriginDescFinishDateAsc(String pppoe);
    Optional<AffectedUsersEntity> findFirstByContractIdOrderByOriginDescFinishDateAsc(Long contractId);
    List<AffectedUsersEntity> findByProtocol(Long protocol);
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM assignment_clients WHERE protocol_id = :protocol", nativeQuery = true)
    Integer deleteByProtocol(Long protocol);

    @Modifying
    @Transactional
    @Query(value = "UPDATE assignment_clients SET finish_date = :finishDate  WHERE protocol_id = :protocol", nativeQuery = true)
    Integer updateUsersByProtocol(@Param("protocol") Long protocol, @Param("finishDate") LocalDateTime finishDate);

    Optional<AffectedUsersEntity> findByContractId(Long contractId);

    Optional<AffectedUsersEntity> findFirstByContractId(Long contractId);

    /**
     * Contratos que ja estao em OUTRO evento aberto: linha de GMUD (existe so durante a janela) ou de massiva
     * cujo protocolo esta 'aberta' em massiva_history. Protocolo orfao/encerrado nao conta.
     */
    @Query(value = "SELECT DISTINCT ac.contract_id FROM assignment_clients ac "
            + "LEFT JOIN massiva_history h ON h.protocol = CAST(ac.protocol_id AS UNSIGNED) "
            + "WHERE ac.contract_id IN (:contractIds) "
            + "AND CAST(ac.protocol_id AS UNSIGNED) <> :protocol "
            + "AND (ac.origin = 'GMUD' OR h.status = 'aberta')", nativeQuery = true)
    List<Number> findContractIdsInOtherOpenEvents(@Param("contractIds") Collection<Long> contractIds,
                                                  @Param("protocol") Long protocol);
}
