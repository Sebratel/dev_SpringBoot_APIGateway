package br.com.sebratel.bff.dto;

import br.com.sebratel.bff.model.entity.UsuarioAfetadoEntity;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateImpactedUsersInputDTO {
    @NotNull(message = "Usuários devem ser adicionados")
    List<UsuarioAfetadoEntity> usuarioAfetadoEntities;
    @NotNull(message = "AssignmentId é Obrigatório")
    Long assignmentId;
}
