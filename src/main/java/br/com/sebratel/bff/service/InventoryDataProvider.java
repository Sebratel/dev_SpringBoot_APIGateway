package br.com.sebratel.bff.service;

import br.com.sebratel.bff.repository.erp.InventoryRepository;
import br.com.sebratel.bff.repository.erp.projections.InventoryProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryDataProvider {

    private final InventoryRepository repository;

    @Cacheable(key = "'estoque'", value = "estoque-geral-tecnicos")
    public List<InventoryProjection> getFullInventory() {
        log.warn("### CACHE MISS: Executando query pesada de estoque no banco de dados ###");
        return repository.findAllEstoqueAgregado();
    }
}