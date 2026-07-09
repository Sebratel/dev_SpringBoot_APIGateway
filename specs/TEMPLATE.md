# Projeto: Sebratel BFF / API Gateway

## 1. Visão Geral
Este projeto é um Backend For Frontend (BFF) que atua como um API Gateway para os serviços da Sebratel. Ele centraliza comunicações com diferentes bases de dados (ERP, Radius, Usuários Afetados) e serviços externos (Elleven, Native).

## 2. Stack Tecnológica
- **Linguagem:** Java 21
- **Framework:** Spring Boot 3.4.3
- **Bancos de Dados:**
  - **PostgreSQL:** Banco de dados do ERP e Usuários Afetados.
  - **MariaDB:** Banco de dados do Radius.
  - **Redis:** Cache de dados.
- **Mensageria:** Spring Kafka
- **Documentação:** SpringDoc OpenAPI (Swagger)
- **Testes:** JUnit 5, Testcontainers, Allure Reports, JaCoCo (Cobertura)
- **Outros:** Selenium/WebDriverManager (Automação), Lombok, MapStruct (Mapeamento)

## 3. Arquitetura e Padrões
- **Camadas:** Controller -> Service -> Repository (JPA/Projections).
- **DTOs:** Obrigatório o uso de DTOs para entrada e saída de dados na API.
- **Exception Handling:** Centralizado em `GlobalExceptionHandler`.
- **Segurança:** OAuth2 Resource Server (JWT).
- **Multitenancy/Multi-DB:** O projeto gerencia múltiplas fontes de dados (Configurações em `br.com.sebratel.bff.config`).
- **Resiliência:** Uso de `@TokenRetry` para chamadas a serviços que dependem de tokens que podem expirar.

## 4. Integrações Principais
- **Elleven:** API de provisionamento e gestão.
- **Native:** Integração de lista de afetados.

## 5. Convenções de Desenvolvimento
- **Nomenclatura:** Classes em PascalCase, métodos e variáveis em camelCase.
- **Idioma:** Código em Inglês (variáveis, classes, métodos), mas termos de domínio de negócio podem aparecer em Português conforme a base de dados Legada.
- **Testes:** Todo novo Service ou Controller deve possuir testes unitários correspondentes em `src/test/java`.
- **Commits:** Seguir o padrão Conventional Commits (`feat:`, `fix:`, `chore:`, etc.).

## 6. Fluxo de Trabalho do Cline
1. Sempre ler este arquivo antes de iniciar qualquer implementação.
2. Seguir os padrões de injeção de dependência do Spring (preferencialmente via construtor).
3. Garantir que as Projections do JPA sejam utilizadas para consultas otimizadas quando não for necessário a entidade completa.
4. Manter a cobertura de testes conforme configurado no `jacoco-maven-plugin`.
