package br.com.sebratel.bff.repository.erp.projections;

public interface InventoryProjection {
    Long getId();
    String getCodigo();
    String getDescricao();
    String getTecnico();
    Integer getPossui();
}