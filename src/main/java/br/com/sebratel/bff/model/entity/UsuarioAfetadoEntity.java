package br.com.sebratel.bff.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @NotNull(message = "O ID do protocolo é obrigatório")
    @Column(name = "protocol_id", nullable = false)
    Long protocol;
    @Column(name = "motive")
    String reason;
    @NotNull(message = "O ID do contrato é obrigatório")
    @Column(name = "contract_id", nullable = false)
    Long contractId;
    @NotNull(message = "A estimativa de finalização é obrigatória")
    @Column(name = "finish_date", nullable = false)
    LocalDateTime finishDate;
    LocalDateTime created;
    @Column(name = "created_by")
    String createdBy;
}
