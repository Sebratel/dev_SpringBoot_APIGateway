package br.com.sebratel.bff.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class ListaDeAfetadosDTO {
    @NotNull(message = "O campo é obrigatório")
    @NotEmpty(message = "A lista não pode ser vazia")
    @JsonProperty("conjuntosAfetados")
    List<ConjuntoAfetadoDTO> conjuntosAfetados;
}
