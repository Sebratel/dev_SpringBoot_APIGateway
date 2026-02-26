package br.com.sebratel.bff.repository.erp.projections.comercial;

import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface PlanilhaInstalacaoProjection {

    @Value("#{target['DATA CRIAÇÃO CONTRATO']}")
    LocalDateTime getDataCriacaoContrato();

    @Value("#{target['CADASTRO CLIENTE']}")
    LocalDateTime getCadastroCliente();

    @Value("#{target['CLIENTES']}")
    String getClienteNome();

    @Value("#{target['CIDADE']}")
    String getCidade();

    @Value("#{target['STATUS CONTRATO']}")
    String getStatusContrato();

    @Value("#{target['STATUS CANCELAMENTO']}")
    String getStatusCancelamento();

    @Value("#{target['CONTRATO']}")
    String getNumeroContrato();

    @Value("#{target['VENDEDOR']}")
    String getVendedorNome();

    @Value("#{target['REGIÃO VENDEDOR']}")
    String getRegiaoVendedor();

    @Value("#{target['VALOR']}")
    BigDecimal getValor();

    @Value("#{target['TECNOLOGIA']}")
    String getTecnologia();

    @Value("#{target['DATA SAÍDA']}")
    LocalDateTime getDataSaida();

    @Value("#{target['RETORNO']}")
    LocalDate getDataRetorno();
}