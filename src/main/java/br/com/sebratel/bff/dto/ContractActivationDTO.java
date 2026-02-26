package br.com.sebratel.bff.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractActivationDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String dataCriacaoContrato;
    private String cadastroCliente;
    private String clientes;
    private String cidade;
    private String statusContrato;
    private String statusCancelamento;
    private String contrato;
    private String vendedor;
    private String regiaoVendedor;
    private Double valor;
    private String tecnologia;
    private String dataAtivacao;
    private String retorno;
}