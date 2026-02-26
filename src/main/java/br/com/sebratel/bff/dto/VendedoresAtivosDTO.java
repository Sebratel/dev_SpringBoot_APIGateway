package br.com.sebratel.bff.dto;

import br.com.sebratel.bff.repository.erp.projections.VendedoresProjection;


public record VendedoresAtivosDTO(String nome, String email) {
    public VendedoresAtivosDTO(VendedoresProjection projection) {
        this(projection.getNomeVendedor(), projection.getEmailVendedor());
    }
}