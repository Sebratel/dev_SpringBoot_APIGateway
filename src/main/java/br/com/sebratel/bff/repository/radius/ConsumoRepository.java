package br.com.sebratel.bff.repository.radius;

import br.com.sebratel.bff.model.RadiusContract;
import br.com.sebratel.bff.repository.radius.projections.ConsumoProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ConsumoRepository extends JpaRepository<RadiusContract, Long> {

    /**
     * Recupera o consumo de dados proporcional ao mês atual para usuários que excederam 1 TB.
     * <p>
     * Lógica de Proporcionalidade:
     * Como sessões RADIUS podem cruzar a virada do mês, a query calcula um 'fator' baseado no tempo:
     * (Tempo da sessão dentro do mês atual) / (Tempo total da sessão).
     * </p>
     * <p>
     * Exemplo: Uma sessão de 10 dias onde 3 dias ocorreram no mês atual terá fator 0.3.
     * O consumo total da sessão é multiplicado por esse fator para garantir um relatório justo.
     * </p>
     *
     * @return Lista de {@link ConsumoProjection} com valores convertidos para Terabytes (10^12 bytes).
     */
    @Query(value = """
        WITH period AS (
            SELECT 
                date_trunc('month', current_date) as inicio,
                date_trunc('month', current_date) + interval '1 month' - interval '1 second' as fim
        ),
        calculo_proporcional AS (
            SELECT 
                username,
                -- Fator de proporcionalidade (Tempo dentro do mês / Tempo total da sessão)
                (EXTRACT(EPOCH FROM (LEAST(COALESCE(acctstoptime, now()), (SELECT fim FROM period)) - GREATEST(acctstarttime, (SELECT inicio FROM period)))) / 
                 NULLIF(EXTRACT(EPOCH FROM (COALESCE(acctstoptime, now()) - acctstarttime)), 0)) AS fator,
                acctinputoctets,
                acctoutputoctets
            FROM public.radacct_convidado
            WHERE acctstarttime <= (SELECT fim FROM period)
              AND COALESCE(acctstoptime, now()) >= (SELECT inicio FROM period)
        )
        SELECT 
            username,
            SUM(acctinputoctets * fator) / 1000000000000.0 AS downloadTb,
            SUM(acctoutputoctets * fator) / 1000000000000.0 AS uploadTb,
            SUM((acctinputoctets + acctoutputoctets) * fator) / 1000000000000.0 AS totalTb
        FROM calculo_proporcional
        GROUP BY username
        -- O filtro agora respeita o consumo proporcional do mês
        HAVING (SUM(acctinputoctets * fator) / 1000000000000.0) > 1.0
        ORDER BY downloadTb DESC
        """, nativeQuery = true)
    List<ConsumoProjection> findConsumoExcedente();


    @Query(value = """
        WITH period AS (
            SELECT 
                date_trunc('month', current_date) as inicio,
                date_trunc('month', current_date) + interval '1 month' - interval '1 second' as fim
        ),
        calculo_proporcional AS (
            SELECT 
                username,
                (EXTRACT(EPOCH FROM (LEAST(COALESCE(acctstoptime, now()), (SELECT fim FROM period)) - GREATEST(acctstarttime, (SELECT inicio FROM period)))) / 
                 NULLIF(EXTRACT(EPOCH FROM (COALESCE(acctstoptime, now()) - acctstarttime)), 0)) AS fator,
                acctinputoctets,
                acctoutputoctets
            FROM public.radacct_convidado
            WHERE acctstarttime <= (SELECT fim FROM period)
              AND COALESCE(acctstoptime, now()) >= (SELECT inicio FROM period)
        )
        SELECT 
            username,
            SUM(acctinputoctets * fator) / 1000000000000.0 AS downloadTb,
            SUM(acctoutputoctets * fator) / 1000000000000.0 AS uploadTb,
            SUM((acctinputoctets + acctoutputoctets) * fator) / 1000000000000.0 AS totalTb
        FROM calculo_proporcional
        GROUP BY username
        HAVING (SUM(acctinputoctets * fator) / 1000000000000.0) > 1.0
        """,
            countQuery = """
            -- Query simplificada para contar quantos usuários excederam o limite
            SELECT COUNT(*) FROM (
                SELECT username 
                FROM public.radacct_convidado 
                WHERE acctstarttime <= date_trunc('month', current_date) + interval '1 month'
                GROUP BY username
                HAVING (SUM(acctinputoctets) / 1000000000000.0) > 1.0
            ) AS total
        """,
            nativeQuery = true)
    Page<ConsumoProjection> findConsumoExcedentePaginado(Pageable pageable);

}