package br.com.sebratel.bff.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "assignment_clients")
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioAfetado {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "user_pppoe")
    String pppoe;
    @Column(name = "protocol_id")
    Long protocol;
    @Column(name = "motive")
    String reason;
    @Column(name = "finish_date")
    LocalDateTime finishDate;
    LocalDateTime created;
    @Column(name = "created_by")
    String createdBy;
}
