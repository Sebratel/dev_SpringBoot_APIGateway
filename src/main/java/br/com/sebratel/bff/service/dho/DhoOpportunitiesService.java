package br.com.sebratel.bff.service.dho;

import br.com.sebratel.bff.dto.DhoOpportunitiesDTO;
import br.com.sebratel.bff.model.entity.dho.DhoOpportunities;
import br.com.sebratel.bff.repository.afetados.dho.DhoOpportunitiesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DhoOpportunitiesService {
    private final DhoOpportunitiesRepository repository;

    public List<DhoOpportunities> findAll() {
        return repository.findAll();
    }

    public List<DhoOpportunitiesDTO> findAllDTOs() {
        return repository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public DhoOpportunities save(DhoOpportunities opportunity) {
        return repository.save(opportunity);
    }

    private DhoOpportunitiesDTO convertToDTO(DhoOpportunities opportunity) {
        return DhoOpportunitiesDTO.builder()
                .id(opportunity.getId())
                .openOpportunityDate(opportunity.getOpenOpportunityDate())
                .candidateName(opportunity.getCandidate() != null ? opportunity.getCandidate().getName() : null)
                .positionName(opportunity.getPosition() != null ? opportunity.getPosition().getName() : null)
                .teamName(opportunity.getTeam() != null ? opportunity.getTeam().getName() : null)
                .departmentName(opportunity.getDepartament() != null ? opportunity.getDepartament().getName() : null)
                .opportunityMotiveName(opportunity.getOpportunityMotive() != null ? opportunity.getOpportunityMotive().getName() : null)
                .replacedPersonName(opportunity.getReplacedPerson() != null ? opportunity.getReplacedPerson().getName() : null)
                .baseOriginName(opportunity.getBaseOrigin() != null ? opportunity.getBaseOrigin().getName() : null)
                .opportunityStatusName(opportunity.getOpportunityStatus() != null ? opportunity.getOpportunityStatus().getName() : null)
                .deadlineSlaDays(opportunity.getDeadlineSlaDays())
                .acceptDate(opportunity.getAcceptDate())
                .responsibleRecruiterName(opportunity.getResponsibleRecruiter() != null ? opportunity.getResponsibleRecruiter().getName() : null)
                .observations(opportunity.getObservations())
                .build();
    }
}
