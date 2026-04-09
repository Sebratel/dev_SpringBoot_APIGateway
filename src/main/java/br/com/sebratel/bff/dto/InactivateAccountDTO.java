package br.com.sebratel.bff.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InactivateAccountDTO {
    @Nullable
    private String accountId;
    private LocalDateTime statusChangedDate;
    private LocalDateTime inactivationDate;
    @JsonProperty("userinfo")
    private InactivateAccountUserInfo userInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InactivateAccountUserInfo {
        private String name;
        private String cpf;
    }
}
