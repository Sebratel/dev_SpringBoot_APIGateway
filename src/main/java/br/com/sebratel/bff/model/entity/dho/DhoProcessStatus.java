package br.com.sebratel.bff.model.entity.dho;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "process_status")
@NoArgsConstructor
@AllArgsConstructor
public class DhoProcessStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "process_status_name")
    private String name;

    @Column(name = "process_status_description")
    private String description;
}
