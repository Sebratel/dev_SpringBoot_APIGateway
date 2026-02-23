package br.com.sebratel.bff.repository.radius;

import br.com.sebratel.bff.model.RadiusContract;
import br.com.sebratel.bff.repository.radius.projections.ConsumoProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ConsumoRepository extends JpaRepository<RadiusContract, Long> {

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
}