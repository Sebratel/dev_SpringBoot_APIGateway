package br.com.sebratel.bff.model.entity.dho;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "situation")
@NoArgsConstructor
@AllArgsConstructor
public class DhoSituation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "situation_name")
    private String name;

    @Column(name = "situation_description")
    private String description;
}
