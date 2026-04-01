package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.TechnicianInventoryDTO;
import br.com.sebratel.bff.repository.erp.projections.InventoryProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryDataProvider inventoryDataProvider;

    public List<TechnicianInventoryDTO> getInventoryByTechnician(String nome) {
        log.info("Solicitando busca de estoque para o técnico: {}", nome);

        List<InventoryProjection> fullInventory = inventoryDataProvider.getFullInventory();

        String nomeFiltro = nome.trim().toUpperCase();

        return fullInventory.stream()
                .filter(view -> view.getTecnico() != null &&
                        view.getTecnico().trim().toUpperCase().equals(nomeFiltro))
                .map(view -> new TechnicianInventoryDTO(
                        view.getCodigo(),
                        view.getDescricao(),
                        view.getTecnico(),
                        view.getPossui() != null ? view.getPossui().longValue() : 0L,
                        view.getId()
                ))
                .toList();
    }
}