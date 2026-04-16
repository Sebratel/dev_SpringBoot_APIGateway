package br.com.sebratel.bff.service.dho;

import br.com.sebratel.bff.model.entity.dho.DhoPeople;
import br.com.sebratel.bff.repository.afetados.dho.DhoPeopleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DhoPeopleService {
    private final DhoPeopleRepository repository;

    public List<DhoPeople> findAll() {
        return repository.findAll();
    }

    public DhoPeople save(DhoPeople person) {
        return repository.save(person);
    }
}
