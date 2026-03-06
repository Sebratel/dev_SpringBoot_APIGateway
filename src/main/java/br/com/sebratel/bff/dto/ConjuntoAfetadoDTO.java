package br.com.sebratel.bff.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class ConjuntoAfetadoDTO {
    @NotNull(message = "A lista de usuários afetados não pode ser nula")
    @NotEmpty(message = "A lista de usuários afetados deve conter pelo menos um item")
    private List<UsuarioAfetadosDTO> usuariosAfetados;

    @NotNull(message = "A data de previsão não pode ser nula")
    private LocalDateTime previsao;

    @NotBlank(message = "A causa não pode estar em branco")
    private String causa;
}
