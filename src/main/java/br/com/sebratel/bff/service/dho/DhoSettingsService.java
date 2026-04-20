package br.com.sebratel.bff.service.dho;

import br.com.sebratel.bff.model.entity.dho.*;
import br.com.sebratel.bff.repository.afetados.dho.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DhoSettingsService {

    private final DhoBaseOriginRepository baseOriginRepository;
    private final DhoDepartamentRepository departamentRepository;
    private final DhoEducationRepository educationRepository;
    private final DhoOpportunityMotiveRepository opportunityMotiveRepository;
    private final DhoOpportunityStatusRepository opportunityStatusRepository;
    private final DhoPositionRepository positionRepository;
    private final DhoProcessStageRepository processStageRepository;
    private final DhoProcessStatusRepository processStatusRepository;
    private final DhoRecruitmentSourceRepository recruitmentSourceRepository;
    private final DhoResignationMotivationRepository resignationMotivationRepository;
    private final DhoResignationTypeRepository resignationTypeRepository;
    private final DhoSituationRepository situationRepository;
    private final DhoTeamRepository teamRepository;

    public Map<String, List<?>> getAllSettings() {
        Map<String, List<?>> settings = new HashMap<>();
        settings.put("baseOrigins", baseOriginRepository.findAll());
        settings.put("departaments", departamentRepository.findAll());
        settings.put("educations", educationRepository.findAll());
        settings.put("opportunityMotives", opportunityMotiveRepository.findAll());
        settings.put("opportunityStatuses", opportunityStatusRepository.findAll());
        settings.put("positions", positionRepository.findAll());
        settings.put("processStages", processStageRepository.findAll());
        settings.put("processStatuses", processStatusRepository.findAll());
        settings.put("recruitmentSources", recruitmentSourceRepository.findAll());
        settings.put("resignationMotivations", resignationMotivationRepository.findAll());
        settings.put("resignationTypes", resignationTypeRepository.findAll());
        settings.put("situations", situationRepository.findAll());
        settings.put("teams", teamRepository.findAll());
        return settings;
    }

    public List<DhoBaseOrigin> findAllBaseOrigins() { return baseOriginRepository.findAll(); }
    public List<DhoDepartament> findAllDepartaments() { return departamentRepository.findAll(); }
    public List<DhoEducation> findAllEducations() { return educationRepository.findAll(); }
    public List<DhoOpportunityMotive> findAllOpportunityMotives() { return opportunityMotiveRepository.findAll(); }
    public List<DhoOpportunityStatus> findAllOpportunityStatuses() { return opportunityStatusRepository.findAll(); }
    public List<DhoPosition> findAllPositions() { return positionRepository.findAll(); }
    public List<DhoProcessStage> findAllProcessStages() { return processStageRepository.findAll(); }
    public List<DhoProcessStatus> findAllProcessStatuses() { return processStatusRepository.findAll(); }
    public List<DhoRecruitmentSource> findAllRecruitmentSources() { return recruitmentSourceRepository.findAll(); }
    public List<DhoResignationMotivation> findAllResignationMotivations() { return resignationMotivationRepository.findAll(); }
    public List<DhoResignationType> findAllResignationTypes() { return resignationTypeRepository.findAll(); }
    public List<DhoSituation> findAllSituations() { return situationRepository.findAll(); }
    public List<DhoTeam> findAllTeams() { return teamRepository.findAll(); }
}
