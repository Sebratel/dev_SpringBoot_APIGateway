package br.com.sebratel.bff.service.dho;

import br.com.sebratel.bff.model.entity.dho.DhoOpportunities;
import br.com.sebratel.bff.repository.afetados.dho.DhoOpportunitiesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DhoOpportunitiesService {
    private final DhoOpportunitiesRepository repository;

    public List<DhoOpportunities> findAll() {
        return repository.findAll();
    }

    public DhoOpportunities save(DhoOpportunities opportunity) {
        return repository.save(opportunity);
    }
}
