package br.com.sebratel.bff.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "massiva_history_splitters")
@NoArgsConstructor
@AllArgsConstructor
public class MassivaHistorySplitterEntity {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "massiva_history_id")
    Long massivaHistoryId;
    @Column(name = "splitter_code")
    String splitterCode;
    @Column(name = "splitter_label")
    String splitterLabel;
    @Column(name = "created_at")
    LocalDateTime createdAt;
}
