package br.com.sebratel.bff.repository.radius.projections;

public interface ConsumoProjection {
    String getUsername();
    Double getDownloadTb();
    Double getUploadTb();
    Double getTotalTb();
}