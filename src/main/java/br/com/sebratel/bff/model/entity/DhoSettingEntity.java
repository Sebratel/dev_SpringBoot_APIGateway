package br.com.sebratel.bff.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "DHO_settings", catalog = "DHO_Application")
@NoArgsConstructor
@AllArgsConstructor
public class DhoSettingEntity {

    @Id
    @Column(name = "CARGO")
    private String cargo;

    @Column(name = "TIME")
    private String time;

    @Column(name = "MOTIVO")
    private String motivo;

    @Column(name = "AREA")
    private String area;

    @Column(name = "LOCAL")
    private String local;

    @Column(name = "STATUS VAGA")
    private String statusVaga;

    @Column(name = "RECRUTADOR")
    private String recrutador;

    @Column(name = "TIPO DE DEMISSÃO")
    private String tipoDeDemissao;

    @Column(name = "MOTIVAÇÃO")
    private String motivacao;

    @Column(name = "SITUAÇÃO")
    private String situacao;

    @Column(name = "ESCOLARIDADE")
    private String escolaridade;

    @Column(name = "ETAPA")
    private String etapa;

    @Column(name = "FONTE")
    private String fonte;

    @Column(name = "GESTOR")
    private String gestor;
}
