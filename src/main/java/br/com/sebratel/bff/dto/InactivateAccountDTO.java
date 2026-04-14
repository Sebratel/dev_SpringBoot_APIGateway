package br.com.sebratel.bff.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    private LocalDateTime statusChangedDate;
    private LocalDateTime inactivationDate;
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
