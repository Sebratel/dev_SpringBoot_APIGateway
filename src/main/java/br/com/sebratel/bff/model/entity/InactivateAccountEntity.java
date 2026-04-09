package br.com.sebratel.bff.model.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;

import java.time.LocalDateTime;

@Data
@Builder
@Entity
@Table(name = "inactivate_users")
@NoArgsConstructor
@AllArgsConstructor
public class InactivateAccountEntity {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id")
    private Long accountId;

    private String name;
    private String cpf;

    private LocalDateTime created;

    @Column(name = "created_by", updatable = false)
    @CreatedBy
    private String createdBy;

    @Column(name = "status_changed_date")
    private LocalDateTime statusChangedDate;

    @Column(name = "inactivation_date")
    private LocalDateTime inactivationDate;
}
