package br.com.sebratel.bff.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class UsuarioAfetadosDTO {
    @NotBlank(message = "O campo pppoe é obrigatório e não pode estar em branco")
    private String pppoe;
}
