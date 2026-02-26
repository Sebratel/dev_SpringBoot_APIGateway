package br.com.sebratel.bff.dto.comercial;


import br.com.sebratel.bff.repository.erp.projections.comercial.ContratoPersonalizadoProjection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContratoPersonalizadoDTO {
    private String nome;
    private String numeroContrato;
    private LocalDate primeiraEmissao;
    private LocalDate pagamentoCliente;
    private LocalDateTime dataCriacao;
    private String contractNumberRaw;
    private String description;
    private String status;

    // Construtor utilitário para converter da Projection
    public ContratoPersonalizadoDTO(ContratoPersonalizadoProjection projection) {
        this.nome = projection.getNome();
        this.numeroContrato = projection.getNumeroContrato();
        this.primeiraEmissao = projection.getPrimeiraEmissao();
        this.pagamentoCliente = projection.getPagamentoCliente();
        this.dataCriacao = projection.getDataCriacao();
        this.contractNumberRaw = projection.getContractNumberRaw();
        this.description = projection.getDescription();
        this.status = projection.getStatus();
    }
}