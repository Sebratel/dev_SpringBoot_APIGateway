package br.com.sebratel.bff.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "dho_opportunities")
@NoArgsConstructor
@AllArgsConstructor
public class DhoOpportunityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "DATA ABERTURA")
    private LocalDate dataAbertura;

    @Column(name = "CARGO")
    private String cargo;

    @Column(name = "MOTIVO")
    private String motivo;

    @Column(name = "NOME SUBSTITUIDO")
    private String nomeSubstituido;

    @Column(name = "TIME")
    private String time;

    @Column(name = "AREA")
    private String area;

    @Column(name = "LOCAL")
    private String local;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "PRAZO")
    private Integer prazo;

    @Column(name = "DATA TERMINO SLA")
    private LocalDate dataTerminoSla;

    @Column(name = "DATA ACEITE")
    private LocalDate dataAceite;

    @Column(name = "SITUACAO PRAZO")
    private String situacaoPrazo;

    @Column(name = "RECRUTADOR")
    private String recrutador;

    @Column(name = "DATA ADMISSAO")
    private LocalDate dataAdmissao;

    @Column(name = "NOME")
    private String nome;

    @Column(name = "POSICAO")
    private String posicao;

    @Column(name = "GESTOR")
    private String gestor;

    @Column(name = "OBSERVACAO")
    private String observacao;
}
