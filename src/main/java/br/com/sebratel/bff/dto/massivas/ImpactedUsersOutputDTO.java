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
    private List<Map<Long, ImpactDetailsOutputDTO>> impactedUsers;

    /** Contratos deixados de fora por ja estarem em outro evento aberto (vazio nas consultas). */
    @JsonProperty("skippedContractIds")
    @Builder.Default
    private List<Long> skippedContractIds = List.of();
}
