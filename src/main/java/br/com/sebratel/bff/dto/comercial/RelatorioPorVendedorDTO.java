package br.com.sebratel.bff.dto.comercial;

import br.com.sebratel.bff.dto.VendedoresAtivosDTO;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class RelatorioPorVendedorDTO {
    VendedoresAtivosDTO dadosDoVendedor;
    List<PrimeiroPaganteMensalDTO> relatorioPorVendedor;
}
