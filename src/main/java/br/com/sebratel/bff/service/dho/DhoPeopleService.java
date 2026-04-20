package br.com.sebratel.bff.service.dho;

import br.com.sebratel.bff.dto.DhoPeopleDTO;
import br.com.sebratel.bff.model.entity.dho.DhoPeople;
import br.com.sebratel.bff.repository.afetados.dho.DhoPeopleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DhoPeopleService {
    private final DhoPeopleRepository repository;

    public List<DhoPeople> findAll() {
        return repository.findAll();
    }

    public List<DhoPeopleDTO> findAllDTOs() {
        return repository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public DhoPeople save(DhoPeople person) {
        return repository.save(person);
    }

    private DhoPeopleDTO convertToDTO(DhoPeople person) {
        return DhoPeopleDTO.builder()
                .id(person.getId())
                .profileImage(person.getProfileImage())
                .registrationNumber(person.getRegistrationNumber())
                .name(person.getName())
                .phoneNumber(person.getPhoneNumber())
                .cpf(person.getCpf())
                .rg(person.getRg())
                .dateBirth(person.getDateBirth())
                .sex(person.getSex())
                .replacement(person.getReplacement())
                .recruitmentData(person.getRecruitmentData())
                .admissionDate(person.getAdmissionDate())
                .observations(person.getObservations())
                .professionalReferences(person.getProfessionalReferences())
                .collaboratorKnowledge(person.getCollaboratorKnowledge())
                .laborLawsuit(person.getLaborLawsuit())
                .criminalBackground(person.getCriminalBackground())
                .externalLink(person.getExternalLink())
                .mindsigthLink(person.getMindsigthLink())
                .cisLink(person.getCisLink())
                .resignationMotivation(person.getResignationMotivation() != null ? person.getResignationMotivation().getName() : null)
                .resignationType(person.getResignationType() != null ? person.getResignationType().getName() : null)
                .build();
    }
}
