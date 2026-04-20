package br.com.sebratel.bff.model.entity.dho;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "recruitment_source")
@NoArgsConstructor
@AllArgsConstructor
public class DhoRecruitmentSource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "recruitment_source_name")
    private String name;

    @Column(name = "recruitment_source_description")
    private String description;
}
