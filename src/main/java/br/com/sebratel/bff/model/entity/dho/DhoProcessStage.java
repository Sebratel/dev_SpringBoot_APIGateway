package br.com.sebratel.bff.model.entity.dho;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "process_stage", schema = "DHO_Application")
@NoArgsConstructor
@AllArgsConstructor
public class DhoProcessStage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "process_stage_name")
    private String name;

    @Column(name = "process_stage_description")
    private String description;
}
