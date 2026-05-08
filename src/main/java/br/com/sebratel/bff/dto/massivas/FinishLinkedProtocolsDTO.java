package br.com.sebratel.bff.dto.massivas;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FinishLinkedProtocolsDTO {
    private Long assignmentLinkado;
    private String protocolDaMassiva;
    private String protocoloLinkado;
    private String tituloDaSolicitacao;
}
