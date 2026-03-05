package br.com.sebratel.bff.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MassivaCriadaOutputDTO {
    @JsonProperty("success")
    String status;
    @JsonProperty("massive_id")
    String id;
}
