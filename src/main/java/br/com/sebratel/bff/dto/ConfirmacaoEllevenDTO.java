package br.com.sebratel.bff.dto;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmacaoEllevenDTO {
    private boolean success;
    @Nullable
    private String message;
}
