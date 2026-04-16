package br.com.sebratel.bff.model.entity.dho;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "base_origin", schema = "DHO_Application")
@NoArgsConstructor
@AllArgsConstructor
public class DhoBaseOrigin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "baseorigin_name")
    private String name;

    @Column(name = "baseorigin_description")
    private String description;
}
