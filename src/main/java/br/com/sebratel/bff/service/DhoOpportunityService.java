package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.DhoOpportunityDTO;
import br.com.sebratel.bff.model.entity.EmployeeEntity;
import br.com.sebratel.bff.repository.afetados.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DhoOpportunityService {

    private final EmployeeRepository repository;

    public List<DhoOpportunityDTO> findAll() {
        log.info("Fetching all active collaborators from database");
        return repository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<DhoOpportunityDTO> findByStatus(String status) {
        log.info("Fetching collaborators with status: {}", status);
        return repository.findAll().stream()
                .filter(e -> status.equalsIgnoreCase(e.getStatus()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private DhoOpportunityDTO convertToDTO(EmployeeEntity entity) {
        return new DhoOpportunityDTO(
                entity.getId(),
                entity.getRegistration(),
                entity.getEmail(),
                entity.getAdmissionDate(),
                entity.getStatus(),
                entity.getOperationalBase(),
                entity.getTeam() != null ? entity.getTeam().getDescription() : null,
                entity.getPosition() != null ? entity.getPosition().getDescription() : null,
                entity.getSupervisorName(),
                entity.getManagerName(),
                entity.getCoordinatorName()
        );
    }
}
