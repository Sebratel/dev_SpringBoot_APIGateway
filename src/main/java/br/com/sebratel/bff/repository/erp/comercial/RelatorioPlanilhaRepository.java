package br.com.sebratel.bff.repository.erp.comercial;

import br.com.sebratel.bff.model.ErpContract;
import br.com.sebratel.bff.repository.erp.projections.comercial.PlanilhaInstalacaoProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

@Repository
public interface RelatorioPlanilhaRepository extends JpaRepository<ErpContract, Long> {
    @Query(value = """
        WITH PERSONALIZADO AS (
                                      SELECT
                                          c.created AS "DATA CRIAÇÃO CONTRATO",
                                          p.created as "CADASTRO CLIENTE",
                                          p.name AS "CLIENTES",
                                          p.city AS "CIDADE",
                                          p2.name AS "VENDEDOR",
                                          rc.city AS "REGIÃO VENDEDOR",
                                          c.v_status AS "STATUS CONTRATO",
                                          c.contract_number AS "CONTRATO",
                                          c.amount AS "VALOR",
                                          CASE
                                              WHEN it.id = '12' OR it.id = '1014' OR it.id = '1136' OR it.id = '249' OR it.id = '1015' THEN MIN(plis.out_date) ELSE a.conclusion_date
                                          END AS  "DATA SAÍDA",
                                          MIN(plis.returned_date) AS "RETORNO",
                                          c.cancellation_motive "STATUS CANCELAMENTO",
                                          CASE
                                              WHEN it.id = '12' OR it.id = '1014' OR it.id = '1136' THEN 'FIBRA'
                                              WHEN it.id = '249' OR it.id = '1015' THEN 'RÁDIO'
                                              ELSE 'TELEFONIA'
                                          END AS "TECNOLOGIA",
                                          ROW_NUMBER() OVER (PARTITION BY c.contract_number ORDER BY a.created ASC) AS ORDENADO
                                      FROM assignments a
                                      INNER JOIN assignment_incidents ai ON a.id = ai.assignment_id
                                      INNER JOIN contract_service_tags cst ON cst.id = ai.contract_service_tag_id
                                      INNER JOIN contracts c ON c.id = cst.contract_id
                                      INNER JOIN incident_types it ON it.id = ai.incident_type_id
                                      INNER JOIN incident_status is2 ON ai.incident_status_id = is2.id
                                      LEFT JOIN people p ON p.id = ai.client_id
                                      LEFT JOIN people p2 ON p2.id = c.seller_1_id
                                      INNER JOIN region_cities rc ON rc.region_id = p2.region_id
                                      LEFT JOIN patrimony_packing_lists ppl ON ppl.contract_service_tag_id = cst.id
                                      LEFT JOIN patrimony_packing_list_items plis ON ppl.id = plis.patrimony_packing_list_id
                                      INNER JOIN people_crm_informations x ON x.person_id = p2.id
                                      WHERE (it.id = '12' OR  --- TEC - Instalação de Fibra
                                          it.id = '1014' OR --- TEC - Instalação de Fibra – CORPORATIVO
                                          it.id = '1136' OR --- TEC - Instalação de Fibra - Cortesia
                                          it.id = '249' OR  --- TEC - Instalação de Rádio
                                          it.id = '275' OR  --- TEC - Instalação Telefonia
                                          it.id = '1011' OR --- TEC - Instalação Telefonia com Portabilidade - Telefonia
                                          it.id = '1015')  --- TEC - Instalação de Radio – CORPORATIVO
                                      AND p2.name = :nome
                                      GROUP BY
                                      a.created,
                                          c.created,
                                          p.created,
                                          p.name,
                                          p.city,
                                          rc.city,
                                          p2.name,
                                          c.v_status,
                                          c.contract_number,
                                          c.amount,
                                          c.cancellation_motive,
                                          plis.returned,
                                          it.id,
                                          a.conclusion_date,
                                          CASE
                                              WHEN it.id = '12' OR it.id = '1014' OR it.id = '1136' THEN 'FIBRA'
                                              WHEN it.id = '249' OR it.id = '1015' THEN 'RÁDIO'
                                              ELSE 'TELEFONIA'
                                          END
                                      ORDER BY p.name ASC
                                  )
                                  SELECT
                                      "DATA CRIAÇÃO CONTRATO",
                                      "CADASTRO CLIENTE",
                                      "CLIENTES",
                                      "CIDADE",
                                      "STATUS CONTRATO",
                                      "STATUS CANCELAMENTO",
                                      "CONTRATO",
                                      "VENDEDOR",
                                      "REGIÃO VENDEDOR",
                                      "VALOR",
                                      "TECNOLOGIA",
                                      "DATA SAÍDA",
                                      "RETORNO"
                                  FROM PERSONALIZADO
                                  WHERE ORDENADO = 1
    """, nativeQuery = true)
    @Transactional(readOnly = true)
    Stream<PlanilhaInstalacaoProjection> findPlanilhaInstalacao(String nome);
}