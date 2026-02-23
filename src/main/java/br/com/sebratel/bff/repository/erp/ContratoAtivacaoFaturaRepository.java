package br.com.sebratel.bff.repository.erp;

import br.com.sebratel.bff.model.ErpContract;
import br.com.sebratel.bff.repository.erp.projections.ContratoAtivacaoFaturaProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContratoAtivacaoFaturaRepository extends JpaRepository<ErpContract, Long> {

    @Query(value = """
        WITH QUERY_CONTRATOS AS (
            SELECT
                p.name AS nome_filtro,
                p2.name as vendedor_filtro,
                co.contract_number AS contrato_filtro,
                fr.expiration_date AS vencimento_filtro,
                x.payment_form_id AS forma,
                fn.title AS banco,
                ROW_NUMBER() OVER (PARTITION by co.contract_number ORDER BY fr.expiration_date ASC) AS ordenado
            FROM erp.financial_receivable_titles fr
            LEFT JOIN erp.people p ON p.id = fr.client_id
            LEFT JOIN erp.financial_receipt_titles x ON x.financial_receivable_title_id = fr.id
            LEFT JOIN erp.contracts co ON co.id = fr.contract_id
            LEFT JOIN erp.people p2 ON p2.id = co.seller_1_id
            LEFT JOIN erp.financers_natures fn ON fn.id = fr.financer_nature_id
            WHERE co.contract_number IS NOT null
            AND fr.deleted IS false
            AND fr.bill_title_id IS NULL 
            AND co.v_status <> 'Cancelado'
        ),
        FILTERED_CONTRATOS AS (
            SELECT nome_filtro, vendedor_filtro, contrato_filtro, vencimento_filtro
            FROM QUERY_CONTRATOS
            WHERE ordenado = 1 AND forma IS NULL AND vencimento_filtro <= CURRENT_DATE
            AND banco <> 'DF - TARIFA BAIXA DE TÍTULO'
        ),
        QUERY_ATIVACOES AS (
            SELECT
                p2.name AS vendedor_ativ,
                p.name as nome_ativ,
                c.contract_number AS contrato_ativ,
                CASE
                    WHEN it.id IN ('12', '1014', '1136', '249', '1015') THEN MIN(plis.out_date) 
                    ELSE a.conclusion_date
                END AS data_ativacao_res,
                ROW_NUMBER() OVER (PARTITION BY c.contract_number ORDER BY a.created ASC) AS ordenado
            FROM erp.assignments a
            LEFT JOIN erp.assignment_incidents ai ON a.id = ai.assignment_id
            LEFT JOIN erp.contract_service_tags cst ON cst.id = ai.contract_service_tag_id
            LEFT JOIN erp.contracts c ON c.id = cst.contract_id
            LEFT JOIN erp.incident_types it ON it.id = ai.incident_type_id
            LEFT JOIN erp.people p ON p.id = ai.client_id
            LEFT JOIN erp.people p2 ON p2.id = c.seller_1_id
            LEFT JOIN erp.patrimony_packing_lists ppl ON ppl.contract_service_tag_id = cst.id
            LEFT JOIN erp.patrimony_packing_list_items plis ON ppl.id = plis.patrimony_packing_list_id
            WHERE it.id IN ('12', '1014', '1136', '249', '275', '1011', '1015')
            AND plis.returned = 0
            GROUP BY p2.name, c.contract_number, it.id, a.conclusion_date, a.created, p.name
        ),
        FILTERED_ATIVACOES AS (
            SELECT contrato_ativ, vendedor_ativ, nome_ativ, data_ativacao_res
            FROM QUERY_ATIVACOES
            WHERE ordenado = 1
        )
        SELECT 
            a.contrato_ativ AS contrato,
            a.vendedor_ativ AS vendedor,
            a.nome_ativ AS nome,
            a.data_ativacao_res AS dataAtivacao,
            c.vencimento_filtro AS vencimento
        FROM FILTERED_ATIVACOES a
        INNER JOIN FILTERED_CONTRATOS c ON 
            a.contrato_ativ = c.contrato_filtro AND 
            a.vendedor_ativ = c.vendedor_filtro AND 
            a.nome_ativ = c.nome_filtro
        ORDER BY c.vencimento_filtro DESC
        """, nativeQuery = true)
    List<ContratoAtivacaoFaturaProjection> findContratosAtivacaoFatura();
}