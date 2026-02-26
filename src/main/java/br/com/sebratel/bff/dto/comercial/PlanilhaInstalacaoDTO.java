package br.com.sebratel.bff.dto.comercial;

import br.com.sebratel.bff.repository.erp.projections.comercial.PlanilhaInstalacaoProjection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanilhaInstalacaoDTO {
    private LocalDateTime dataCriacaoContrato;
    private LocalDateTime cadastroCliente;
    private String clienteNome;
    private String cidade;
    private String statusContrato;
    private String statusCancelamento;
    private String numeroContrato;
    private String vendedorNome;
    private String regiaoVendedor;
    private BigDecimal valor;
    private String tecnologia;
    private LocalDateTime dataSaida;
    private LocalDate dataRetorno;

    public PlanilhaInstalacaoDTO(PlanilhaInstalacaoProjection projection) {
        this.dataCriacaoContrato = projection.getDataCriacaoContrato();
        this.cadastroCliente = projection.getCadastroCliente();
        this.clienteNome = projection.getClienteNome();
        this.cidade = projection.getCidade();
        this.statusContrato = projection.getStatusContrato();
        this.statusCancelamento = projection.getStatusCancelamento();
        this.numeroContrato = projection.getNumeroContrato();
        this.vendedorNome = projection.getVendedorNome();
        this.regiaoVendedor = projection.getRegiaoVendedor();
        this.valor = projection.getValor();
        this.tecnologia = projection.getTecnologia();
        this.dataSaida = projection.getDataSaida();
        this.dataRetorno = projection.getDataRetorno();
    }

}