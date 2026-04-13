package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.DhoOpportunityDTO;
import br.com.sebratel.bff.model.entity.DhoOpportunityEntity;
import br.com.sebratel.bff.repository.afetados.DhoOpportunityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DhoOpportunityService {

    private final DhoOpportunityRepository repository;

    public List<DhoOpportunityDTO> findAll() {
        log.info("Fetching all DHO opportunities from database");
        return repository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<DhoOpportunityDTO> findByStatus(String status) {
        log.info("Fetching DHO opportunities with status: {}", status);
        // Note: In a real scenario, you'd add a method to the repository
        // For simplicity now, we filter in memory or we can add the repository method
        return repository.findAll().stream()
                .filter(e -> status.equalsIgnoreCase(e.getStatus()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private DhoOpportunityDTO convertToDTO(DhoOpportunityEntity entity) {
        return new DhoOpportunityDTO(
                entity.getId(),
                entity.getDataAbertura(),
                entity.getCargo(),
                entity.getMotivo(),
                entity.getNomeSubstituido(),
                entity.getTime(),
                entity.getArea(),
                entity.getLocal(),
                entity.getStatus(),
                entity.getPrazo(),
                entity.getDataTerminoSla(),
                entity.getDataAceite(),
                entity.getSituacaoPrazo(),
                entity.getRecrutador(),
                entity.getDataAdmissao(),
                entity.getNome(),
                entity.getPosicao(),
                entity.getGestor(),
                entity.getObservacao()
        );
    }
}
