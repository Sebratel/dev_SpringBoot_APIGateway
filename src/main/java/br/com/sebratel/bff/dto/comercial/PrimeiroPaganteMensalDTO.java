package br.com.sebratel.bff.dto.comercial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrimeiroPaganteMensalDTO {

    private String nome;
    private String numeroContrato;
    private LocalDate primeiraEmissao;
    private LocalDate pagamentoCliente;
    private LocalDateTime dataCriacao;
    private String description;
    private String status;

    private LocalDateTime cadastroCliente;
    private String cidade;
    private String vendedorNome;
    private String regiaoVendedor;
    private BigDecimal valor;
    private String tecnologia;
    private LocalDateTime dataSaida;
    private LocalDateTime dataRetorno;
    private String statusCancelamento;
}