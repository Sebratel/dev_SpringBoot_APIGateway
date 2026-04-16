package br.com.sebratel.bff.model.entity.dho;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "team", schema = "DHO_Application")
@NoArgsConstructor
@AllArgsConstructor
public class DhoTeam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "team_name")
    private String name;

    @Column(name = "team_description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private DhoPeople manager;
}
