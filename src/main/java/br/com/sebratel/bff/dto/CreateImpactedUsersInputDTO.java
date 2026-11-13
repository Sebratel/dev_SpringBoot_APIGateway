package br.com.sebratel.bff.dto;

import br.com.sebratel.bff.enums.ClientType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateImpactedUsersInputDTO {
    @NotEmpty(message = "A lista de usuários afetados não pode estar vazia")
    @Valid
    List<AffectedUserRequestDTO> usuarioAfetadoEntities;
    @NotNull(message = "O ID do assignment (assignmentId) é obrigatório")
    Long assignmentId;
    /**
     * Quando true, grava tambem os clientes que ja estao em OUTRO evento aberto (por padrao ficam de fora:
     * o cliente ja esta sinalizado e nao pode entrar duas vezes).
     */
    boolean includeConflicting;
}
