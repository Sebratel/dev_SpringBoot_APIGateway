package br.com.sebratel.bff.dto.massivas;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class ImpactedUsersDTO {
    @NotNull(message = "O campo é obrigatório")
    @NotEmpty(message = "A lista não pode ser vazia")
    @JsonProperty("impactedUsers")
    private Map<String, ImpactDetailsDTO>[] impactedUsers;
}
