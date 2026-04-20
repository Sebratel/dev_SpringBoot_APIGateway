package br.com.sebratel.bff.model.entity.dho;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "opportunity_motive")
@NoArgsConstructor
@AllArgsConstructor
public class DhoOpportunityMotive {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "opportunity_motive_name")
    private String name;

    @Column(name = "opportunity_motive_description")
    private String description;
}
