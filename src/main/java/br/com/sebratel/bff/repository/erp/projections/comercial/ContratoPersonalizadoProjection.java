package br.com.sebratel.bff.repository.erp.projections.comercial;

import org.springframework.beans.factory.annotation.Value;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ContratoPersonalizadoProjection {

    @Value("#{target['NOME']}")
    String getNome();

    @Value("#{target['NÚMERO CONTRATO']}")
    String getNumeroContrato();

    @Value("#{target['PRIMEIRA EMISSÃO']}")
    LocalDate getPrimeiraEmissao();

    @Value("#{target['PAGAMENTO CLIENTE']}")
    LocalDate getPagamentoCliente();

    @Value("#{target['DATA CRIAÇÃO']}")
    LocalDateTime getDataCriacao();

    @Value("#{target['CONTRACTNUMBER']}")
    String getContractNumberRaw();

    @Value("#{target['DESCRIPTION']}")
    String getDescription();

    @Value("#{target['STATUS']}")
    String getStatus();
}