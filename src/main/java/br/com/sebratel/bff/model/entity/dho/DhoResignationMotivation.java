package br.com.sebratel.bff.model.entity.dho;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "resignation_motivation")
@NoArgsConstructor
@AllArgsConstructor
public class DhoResignationMotivation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "resignation_motivation_name")
    private String name;

    @Column(name = "resignation_motivation_description")
    private String description;
}
