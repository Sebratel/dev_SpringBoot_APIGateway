package br.com.sebratel.bff.repository.erp;

import br.com.sebratel.bff.repository.erp.projections.ContractActivationProjection;
import org.junit.jupiter.api.BeforeEach;
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
class ContractActivationRepositoryTest {

    @Autowired
    private ContractActivationRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        // Limpa tudo para evitar conflitos de tabelas já existentes
        jdbcTemplate.execute("DROP ALL OBJECTS");

        // Recria os schemas que o Hibernate espera encontrar
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS erp");
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS radius");

        // Criação manual das tabelas para a Native Query
        jdbcTemplate.execute("CREATE TABLE erp.people (id SERIAL PRIMARY KEY, name VARCHAR(255), city VARCHAR(255))");
        jdbcTemplate.execute("CREATE TABLE erp.contracts (id SERIAL PRIMARY KEY, contract_number VARCHAR(50), created TIMESTAMP, v_status INT, seller_1_id INT, cancellation_motive VARCHAR(255))");
        jdbcTemplate.execute("CREATE TABLE erp.financial_receivable_titles (id SERIAL PRIMARY KEY, contract_id INT)");
        jdbcTemplate.execute("CREATE TABLE bank_accounts (id SERIAL PRIMARY KEY, description VARCHAR(255))");
        jdbcTemplate.execute("CREATE TABLE erp.financial_receipt_titles (id SERIAL PRIMARY KEY, client_id INT, financial_receivable_title_id INT, bank_account_id INT, client_paid_date TIMESTAMP)");

        jdbcTemplate.execute("CREATE TABLE assignments (id SERIAL PRIMARY KEY, created TIMESTAMP, conclusion_date TIMESTAMP)");
        jdbcTemplate.execute("CREATE TABLE assignment_incidents (id SERIAL PRIMARY KEY, assignment_id INT, contract_service_tag_id INT, incident_type_id VARCHAR(50), client_id INT)");
        jdbcTemplate.execute("CREATE TABLE contract_service_tags (id SERIAL PRIMARY KEY, contract_id INT)");
        jdbcTemplate.execute("CREATE TABLE incident_types (id VARCHAR(50) PRIMARY KEY)");
        jdbcTemplate.execute("CREATE TABLE patrimony_packing_lists (id SERIAL PRIMARY KEY, contract_service_tag_id INT)");
        jdbcTemplate.execute("CREATE TABLE patrimony_packing_list_items (id SERIAL PRIMARY KEY, patrimony_packing_list_id INT, out_date TIMESTAMP)");

        // Cria as tabelas espelho (sem o prefixo erp.) se a query as chamar diretamente
        jdbcTemplate.execute("CREATE TABLE contracts AS SELECT * FROM erp.contracts WHERE 1=0");
        jdbcTemplate.execute("CREATE TABLE people AS SELECT * FROM erp.people WHERE 1=0");
    }

    @Test
    @DisplayName("Deve retornar dados mesclados de ativação")
    void findMergedContractData_Sucesso() {
        jdbcTemplate.update("INSERT INTO incident_types (id) VALUES ('12')");
        jdbcTemplate.update("INSERT INTO erp.people (id, name, city) VALUES (1, 'Cliente Teste', 'Porto Alegre')");
        jdbcTemplate.update("INSERT INTO erp.contracts (id, contract_number, created, v_status) VALUES (10, 'CTR-999', CURRENT_TIMESTAMP, 1)");
        jdbcTemplate.update("INSERT INTO contract_service_tags (id, contract_id) VALUES (100, 10)");
        jdbcTemplate.update("INSERT INTO assignments (id, created, conclusion_date) VALUES (50, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)");
        jdbcTemplate.update("INSERT INTO assignment_incidents (assignment_id, contract_service_tag_id, incident_type_id, client_id) VALUES (50, 100, '12', 1)");

        jdbcTemplate.execute("INSERT INTO people SELECT * FROM erp.people");
        jdbcTemplate.execute("INSERT INTO contracts SELECT * FROM erp.contracts");

        List<ContractActivationProjection> result = repository.findMergedContractData();

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getContrato()).isEqualTo("CTR-999");
    }
}