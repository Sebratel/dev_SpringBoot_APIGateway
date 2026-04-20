package br.com.sebratel.bff.model.entity.dho;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "departament")
@NoArgsConstructor
@AllArgsConstructor
public class DhoDepartament {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "departament_name")
    private String name;

    @Column(name = "departament_description")
    private String description;
}
