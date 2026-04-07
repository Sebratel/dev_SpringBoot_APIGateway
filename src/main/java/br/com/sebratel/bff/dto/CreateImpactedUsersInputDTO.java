package br.com.sebratel.bff.dto;

import br.com.sebratel.bff.model.entity.UsuarioAfetadoEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateImpactedUsersInputDTO {
    List<UsuarioAfetadoEntity> usuarioAfetadoEntities;
    Long assignmentId;
}
