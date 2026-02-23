package br.com.sebratel.bff.repository.radius;


import br.com.sebratel.bff.model.RadiusContract;
import br.com.sebratel.bff.repository.radius.projections.UltimaConexaoProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UltimaConexaoRepository extends JpaRepository<RadiusContract, Long> {

    @Query(value = """
        WITH UltimoInicio AS (
            SELECT 
                username, 
                MAX(radacctid) AS ultimo_inicio
            FROM 
                public.radacct_convidado
            GROUP BY 
                username
        )
        SELECT
            rc.username AS usuario,
            rc.acctstarttime AS inicio,
            rc.acctupdatetime AS atualizado,
            rc.acctstoptime AS pausado,
            rc.acctoutputoctets AS recebendo,
            rc.acctinputoctets AS enviando,
            rc.framedipaddress AS ipConexao
        FROM 
            public.radacct_convidado rc
        JOIN 
            UltimoInicio ui ON rc.username = ui.username
        WHERE 
            rc.acctstoptime IS NULL
            AND rc.radacctid = ui.ultimo_inicio
        """, nativeQuery = true)
    List<UltimaConexaoProjection> findUltimasConexoesAtivas();
}