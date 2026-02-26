package br.com.sebratel.bff.repository.erp;

import br.com.sebratel.bff.repository.erp.projections.AquisicaoProjection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AquisicaoRepositoryTest {

    @Autowired
    private AquisicaoRepository aquisicaoRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Deve retornar aquisições pendentes quando existirem registros no banco")
    void findAquisicoesPendentes_Sucesso() {
        // GIVEN: Criamos a estrutura mínima de tabelas e dados via SQL (pois são native queries)
        setupData();

        // WHEN
        List<AquisicaoProjection> resultado = aquisicaoRepository.findAquisicoesPendentes();

        // THEN
        assertThat(resultado).isNotEmpty();
        AquisicaoProjection projection = resultado.get(0);

        // Valida se os aliases do SQL estão mapeando corretamente para os métodos da Projection
        assertThat(projection.getCodigo()).isEqualTo("123");
        assertThat(projection.getProduto()).isEqualTo("Cabo de Rede");
        assertThat(projection.getRequisitadoPor()).isEqualTo("João Silva");
        assertThat(projection.getBase()).isEqualTo("SEDE - PORTO ALEGRE");
        assertThat(projection.getStatus()).isEqualTo("Aguardando Entrega");
    }

    private void setupData() {
        // Criando tabelas necessárias para a Native Query (caso não existam no esquema de teste)
        jdbcTemplate.execute("CREATE TABLE people (id SERIAL PRIMARY KEY, name VARCHAR(255))");
        jdbcTemplate.execute("CREATE TABLE service_products (id SERIAL PRIMARY KEY, code VARCHAR(50), title VARCHAR(255))");
        jdbcTemplate.execute("CREATE TABLE product_acquisition_requests (id SERIAL PRIMARY KEY, code VARCHAR(50), date DATE, receipt_date_prevision DATE, status INT, person_id INT, company_place_id INT, observation VARCHAR(255), deleted BOOLEAN)");
        jdbcTemplate.execute("CREATE TABLE product_acquisition_request_items (id SERIAL PRIMARY KEY, product_acquisition_request_id INT, service_product_id INT, units INT)");
        jdbcTemplate.execute("CREATE TABLE company_place_business_units (company_place_id INT)");
        jdbcTemplate.execute("CREATE TABLE units_measures (code VARCHAR(50))");

        // Inserindo dados de teste
        jdbcTemplate.update("INSERT INTO people (id, name) VALUES (1, 'João Silva')");
        jdbcTemplate.update("INSERT INTO service_products (id, code, title) VALUES (1, '123', 'Cabo de Rede')");
        jdbcTemplate.update("INSERT INTO product_acquisition_requests (id, date, status, person_id, company_place_id, observation, deleted) " +
                "VALUES (100, CURRENT_DATE, 4, 1, 1, 'Entrega em NAVEGANTES', false)");
        jdbcTemplate.update("INSERT INTO product_acquisition_request_items (product_acquisition_request_id, service_product_id, units) " +
                "VALUES (100, 1, 10)");
    }
}