package br.com.sebratel.bff.repository.erp.comercial;

import br.com.sebratel.bff.repository.erp.projections.comercial.PlanilhaInstalacaoProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class RelatorioPlanilhaRepositoryTest {

    @Autowired
    private RelatorioPlanilhaRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        jdbcTemplate.execute("DROP ALL OBJECTS");

        jdbcTemplate.execute("CREATE TABLE people (id SERIAL PRIMARY KEY, name VARCHAR(255), city VARCHAR(255), region_id INT, created TIMESTAMP)");
        jdbcTemplate.execute("CREATE TABLE contracts (id SERIAL PRIMARY KEY, created TIMESTAMP, v_status INT, contract_number VARCHAR(50), amount DECIMAL, seller_1_id INT, cancellation_motive VARCHAR(255))");
        jdbcTemplate.execute("CREATE TABLE assignments (id SERIAL PRIMARY KEY, created TIMESTAMP, conclusion_date TIMESTAMP)");
        jdbcTemplate.execute("CREATE TABLE assignment_incidents (id SERIAL PRIMARY KEY, assignment_id INT, contract_service_tag_id INT, incident_type_id VARCHAR(50), incident_status_id INT, client_id INT)");
        jdbcTemplate.execute("CREATE TABLE contract_service_tags (id SERIAL PRIMARY KEY, contract_id INT)");
        jdbcTemplate.execute("CREATE TABLE incident_types (id VARCHAR(50) PRIMARY KEY)");
        jdbcTemplate.execute("CREATE TABLE incident_status (id SERIAL PRIMARY KEY)");
        jdbcTemplate.execute("CREATE TABLE region_cities (region_id INT, city VARCHAR(255))");
        jdbcTemplate.execute("CREATE TABLE patrimony_packing_lists (id SERIAL PRIMARY KEY, contract_service_tag_id INT)");
        jdbcTemplate.execute("CREATE TABLE patrimony_packing_list_items (id SERIAL PRIMARY KEY, patrimony_packing_list_id INT, out_date TIMESTAMP, returned_date TIMESTAMP, returned BOOLEAN)");
        jdbcTemplate.execute("CREATE TABLE people_crm_informations (person_id INT)");
    }

    @Test
    @DisplayName("Deve executar a query nativa e garantir cobertura do repositório")
    void deveExecutarQueryEMapearProjection() {
        String vendedorAlvo = "Vendedor Cobertura";

        jdbcTemplate.update("INSERT INTO incident_types (id) VALUES ('12')");
        jdbcTemplate.update("INSERT INTO people (id, name, city, created) VALUES (1, 'Cliente Teste', 'Porto Alegre', CURRENT_TIMESTAMP)");
        jdbcTemplate.update("INSERT INTO people (id, name, region_id) VALUES (2, ?, 100)", vendedorAlvo);
        jdbcTemplate.update("INSERT INTO region_cities (region_id, city) VALUES (100, 'Regiao Teste')");
        jdbcTemplate.update("INSERT INTO contracts (id, created, v_status, contract_number, amount, seller_1_id) VALUES (10, CURRENT_TIMESTAMP, 1, 'CT-100', 100.0, 2)");
        jdbcTemplate.update("INSERT INTO contract_service_tags (id, contract_id) VALUES (100, 10)");
        jdbcTemplate.update("INSERT INTO assignments (id, created, conclusion_date) VALUES (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)");
        jdbcTemplate.update("INSERT INTO assignment_incidents (assignment_id, contract_service_tag_id, incident_type_id, client_id) VALUES (1, 100, '12', 1)");
        jdbcTemplate.update("INSERT INTO people_crm_informations (person_id) VALUES (2)");

        try (Stream<PlanilhaInstalacaoProjection> resultStream = repository.findPlanilhaInstalacao(vendedorAlvo)) {
            List<PlanilhaInstalacaoProjection> result = resultStream.toList();

            assertThat(result).isNotEmpty();

            PlanilhaInstalacaoProjection projection = result.get(0);
            assertThat(projection.getVendedorNome()).isEqualTo(vendedorAlvo);
            assertThat(projection.getClienteNome()).isEqualTo("Cliente Teste");
            assertThat(projection.getTecnologia()).isEqualTo("FIBRA");
        }
    }

    @Test
    @DisplayName("Deve garantir cobertura para retorno vazio")
    void deveGarantirCoberturaParaRetornoVazio() {
        try (Stream<PlanilhaInstalacaoProjection> resultStream = repository.findPlanilhaInstalacao("Ninguem")) {
            assertThat(resultStream.toList()).isEmpty();
        }
    }
}