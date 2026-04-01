package br.com.sebratel.bff.model.entity;

import br.com.sebratel.bff.enums.ClientType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "assignment_clients")
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioAfetadoEntity {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "user_pppoe")
    String pppoe;
    @NotNull(message = "O ID do protocolo é obrigatório (protocol)")
    @Column(name = "protocol_id", nullable = false)
    Long protocol;
    @Column(name = "motive")
    String reason;
    @Enumerated(EnumType.STRING)
    @Column(name="corporate_client")
    ClientType ClientType;
    @NotNull(message = "O ID do contrato é obrigatório (contractId)")
    @Column(name = "contract_id", nullable = false)
    Long contractId;
    @NotNull(message = "A estimativa de finalização é obrigatória (finishDate)")
    @Column(name = "finish_date", nullable = false)
    LocalDateTime finishDate;
    LocalDateTime created;
    @Column(name = "created_by", updatable = false, nullable = false)
    @CreatedBy
    private String createdBy;
}
