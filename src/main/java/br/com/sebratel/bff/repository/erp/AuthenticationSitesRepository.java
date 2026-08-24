package br.com.sebratel.bff.repository.erp;

import br.com.sebratel.bff.model.entity.AuthenticationSiteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthenticationSitesRepository extends JpaRepository<AuthenticationSiteEntity, Long> {
    List<AuthenticationSiteEntity> findByTitle(String title);

    /** Busca por título (contém, case-insensitive) para o seletor de site — limita o retorno. */
    List<AuthenticationSiteEntity> findTop50ByTitleContainingIgnoreCaseOrderByTitleAsc(String title);
}
