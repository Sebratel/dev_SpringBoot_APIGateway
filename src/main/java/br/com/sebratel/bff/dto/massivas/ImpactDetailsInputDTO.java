package br.com.sebratel.bff.dto.massivas;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ImpactDetailsInputDTO {
    @NotBlank(message = "A causa não pode estar em branco")
    String reason;
    @NotNull(message = "A data de previsão não pode ser nula")
    LocalDateTime estimateTimeOfRestoration;
}
