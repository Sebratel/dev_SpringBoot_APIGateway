package br.com.sebratel.bff.model.entity.dho;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "resignation_type")
@NoArgsConstructor
@AllArgsConstructor
public class DhoResignationType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "resignation_type_name")
    private String name;

    @Column(name = "resignation_type_description")
    private String description;
}
