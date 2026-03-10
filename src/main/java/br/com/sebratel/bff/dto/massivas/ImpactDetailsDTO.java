package br.com.sebratel.bff.dto.massivas;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ImpactDetailsDTO {
    @NotBlank(message = "A causa não pode estar em branco")
    String reason;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    @NotNull(message = "A data de previsão não pode ser nula")
    LocalDateTime estimateTimeOfRestoration;
}
