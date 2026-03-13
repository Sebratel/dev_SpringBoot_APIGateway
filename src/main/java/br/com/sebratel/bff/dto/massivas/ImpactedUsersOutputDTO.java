package br.com.sebratel.bff.dto.massivas;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class ImpactedUsersOutputDTO {
    @NotNull(message = "O campo é obrigatório")
    @NotEmpty(message = "A lista não pode ser vazia")
    @JsonProperty("impactedUsers")
    private List<Map<String, ImpactDetailsOutputDTO>> impactedUsers;
}
