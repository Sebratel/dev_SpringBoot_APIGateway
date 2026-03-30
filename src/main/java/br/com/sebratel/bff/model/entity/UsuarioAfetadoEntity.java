package br.com.sebratel.bff.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
    @Size(min = 15, message = "O motivo deve ter pelo menos 15 caracteres (motive)")
    String reason;
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
