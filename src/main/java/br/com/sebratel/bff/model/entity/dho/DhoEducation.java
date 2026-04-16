package br.com.sebratel.bff.model.entity.dho;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "education", schema = "DHO_Application")
@NoArgsConstructor
@AllArgsConstructor
public class DhoEducation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "education_name")
    private String name;

    @Column(name = "education_description")
    private String description;
}
