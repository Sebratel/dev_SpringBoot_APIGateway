package br.com.sebratel.bff.repository.erp.comercial;

import br.com.sebratel.bff.repository.erp.projections.comercial.ContratoPersonalizadoProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ContratoPersonalizadoRepositoryTest {

    @Autowired
    private ContratoPersonalizadoRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        // Resolve o erro de "Table already exists" limpando o banco antes de criar o schema manual
        jdbcTemplate.execute("DROP ALL OBJECTS");

        jdbcTemplate.execute("CREATE SCHEMA erp");
        jdbcTemplate.execute("CREATE TABLE erp.people (id SERIAL PRIMARY KEY, name VARCHAR(255))");
        jdbcTemplate.execute("CREATE TABLE erp.contracts (id SERIAL PRIMARY KEY, contract_number VARCHAR(50), description VARCHAR(255), v_status INT, created TIMESTAMP)");
        jdbcTemplate.execute("CREATE TABLE erp.financial_receivable_titles (id SERIAL PRIMARY KEY, contract_id INT, issue_date DATE)");
        jdbcTemplate.execute("CREATE TABLE bank_accounts (id SERIAL PRIMARY KEY, description VARCHAR(255))");
        jdbcTemplate.execute("CREATE TABLE erp.financial_receipt_titles (id SERIAL PRIMARY KEY, client_id INT, financial_receivable_title_id INT, bank_account_id INT, client_paid_date TIMESTAMP)");
        jdbcTemplate.execute("CREATE TABLE erp.authentication_contracts (id SERIAL PRIMARY KEY, contract_id INT, service_product_id INT)");
        jdbcTemplate.execute("CREATE TABLE erp.service_products (id SERIAL PRIMARY KEY, title VARCHAR(255))");
    }

    @Test
    @DisplayName("Deve retornar contratos personalizados filtrados por cliente e data")
    void findContratosPersonalizados_Sucesso() {
        LocalDateTime inicio = LocalDateTime.now().minusDays(10);
        LocalDateTime fim = LocalDateTime.now().plusDays(10);
        String nomeCliente = "Cliente Teste";

        jdbcTemplate.update("INSERT INTO erp.people (id, name) VALUES (1, ?)", nomeCliente);
        jdbcTemplate.update("INSERT INTO erp.contracts (id, contract_number, description, v_status, created) VALUES (10, 'CONTRATO-001', 'Desc', 1, CURRENT_TIMESTAMP)");
        jdbcTemplate.update("INSERT INTO erp.financial_receivable_titles (id, contract_id, issue_date) VALUES (100, 10, CURRENT_DATE)");
        jdbcTemplate.update("INSERT INTO bank_accounts (id, description) VALUES (1, 'Banco Real')");
        jdbcTemplate.update("INSERT INTO erp.financial_receipt_titles (id, client_id, financial_receivable_title_id, bank_account_id, client_paid_date) VALUES (1000, 1, 100, 1, CURRENT_TIMESTAMP)");
        jdbcTemplate.update("INSERT INTO erp.authentication_contracts (id, contract_id, service_product_id) VALUES (1, 10, 1)");
        jdbcTemplate.update("INSERT INTO erp.service_products (id, title) VALUES (1, 'Internet Fibra')");

        List<ContratoPersonalizadoProjection> resultado = repository.findContratosPersonalizados(inicio, fim, List.of(nomeCliente));

        assertThat(resultado).isNotEmpty();
        assertThat(resultado.get(0).getNome()).isEqualTo(nomeCliente);
        assertThat(resultado.get(0).getNumeroContrato()).isEqualTo("CONTRATO-001");
    }

    @Test
    @DisplayName("Não deve retornar contratos se o cliente estiver fora da lista")
    void findContratosPersonalizados_FiltroInvalido() {
        LocalDateTime inicio = LocalDateTime.now().minusDays(10);
        LocalDateTime fim = LocalDateTime.now().plusDays(10);

        List<ContratoPersonalizadoProjection> resultado = repository.findContratosPersonalizados(inicio, fim, List.of("Outro Cliente"));

        assertThat(resultado).isEmpty();
    }
}