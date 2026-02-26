package br.com.sebratel.bff.repository.erp;

import br.com.sebratel.bff.model.ErpContract;
import br.com.sebratel.bff.repository.erp.projections.ContractActivationProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractActivationRepository extends JpaRepository<ErpContract, Long> {

    @Query(value = """
        WITH FINANCEIRO AS (
            SELECT 
                p.name AS nome_cliente, 
                co.contract_number AS num_contrato,
                co.created AS data_criacao_fin,
                ROW_NUMBER() OVER (PARTITION BY p.name, co.contract_number ORDER BY x.client_paid_date ASC) AS ordenado_fin
            FROM erp.financial_receipt_titles x
            LEFT JOIN erp.people p ON p.id = x.client_id
            LEFT JOIN erp.financial_receivable_titles fr ON x.financial_receivable_title_id = fr.id
            LEFT JOIN erp.contracts co ON co.id = fr.contract_id
            LEFT JOIN bank_accounts b ON x.bank_account_id = b.id
            WHERE co.contract_number IS NOT NULL
            AND b.description <> 'Conta Transitória (Baixa/Perda) - YES'
        ),
        PLANILHA AS (
            SELECT 
                c.created AS data_criacao_contrato,
                p.name AS clientes,
                p.city AS cidade,
                p2.name AS vendedor,
                c.v_status AS status_contrato,
                c.contract_number AS contrato,
                c.cancellation_motive AS status_cancelamento,
                CASE 
                    WHEN it.id IN ('12', '1014', '1136') THEN 'FIBRA' 
                    WHEN it.id IN ('249', '1015') THEN 'RÁDIO' 
                    ELSE 'TELEFONIA'
                END AS tecnologia,
                CASE 
                    WHEN it.id IN ('12', '1014', '1136', '249', '1015') THEN MIN(plis.out_date) 
                    ELSE a.conclusion_date 
                END AS data_ativacao,
                ROW_NUMBER() OVER (PARTITION BY c.contract_number ORDER BY a.created ASC) AS ordenado_plan
            FROM assignments a 
            LEFT JOIN assignment_incidents ai ON a.id = ai.assignment_id 
            LEFT JOIN contract_service_tags cst ON cst.id = ai.contract_service_tag_id 
            LEFT JOIN contracts c ON c.id = cst.contract_id
            LEFT JOIN incident_types it ON it.id = ai.incident_type_id
            LEFT JOIN people p ON p.id = ai.client_id
            LEFT JOIN people p2 ON p2.id = c.seller_1_id 
            LEFT JOIN patrimony_packing_lists ppl ON ppl.contract_service_tag_id = cst.id 
            LEFT JOIN patrimony_packing_list_items plis ON ppl.id = plis.patrimony_packing_list_id
            WHERE it.id IN ('12', '1014', '1136', '249', '275', '1011', '1015')
            AND c.created > (CURRENT_DATE - INTERVAL '11' MONTH)
            GROUP BY a.created, c.created, p.name, p.city, p2.name, c.v_status, c.contract_number, c.cancellation_motive, it.id, a.conclusion_date
        )
        SELECT 
            pl.data_criacao_contrato AS dataCriacaoContrato,
            pl.data_ativacao AS dataAtivacao,
            pl.contrato AS contrato,
            pl.vendedor AS vendedor,
            pl.clientes AS clientes,
            pl.tecnologia AS tecnologia,
            pl.status_contrato AS statusContrato,
            pl.status_cancelamento AS statusCancelamento
        FROM PLANILHA pl
        LEFT JOIN FINANCEIRO fin ON fin.nome_cliente = pl.clientes 
             AND fin.data_criacao_fin = pl.data_criacao_contrato
             AND fin.ordenado_fin = 1
        WHERE pl.ordenado_plan = 1
        ORDER BY pl.clientes ASC
        """, nativeQuery = true)
    List<ContractActivationProjection> findMergedContractData();

}