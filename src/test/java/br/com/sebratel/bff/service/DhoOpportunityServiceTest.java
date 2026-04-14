package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.DhoOpportunityDTO;
import br.com.sebratel.bff.model.entity.DhoOpportunityEntity;
import br.com.sebratel.bff.repository.afetados.DhoOpportunityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DhoOpportunityServiceTest {

    @Mock
    private DhoOpportunityRepository repository;

    @InjectMocks
    private DhoOpportunityService service;

    private DhoOpportunityEntity entity;

    @BeforeEach
    void setUp() {
        entity = new DhoOpportunityEntity();
        entity.setId(1);
        entity.setDataAbertura(LocalDate.now());
        entity.setCargo("Developer");
        entity.setStatus("OPEN");
        entity.setArea("Tech");
        entity.setLocal("Florianópolis");
    }

    @Test
    void findAll_ShouldReturnList() {
        when(repository.findAll()).thenReturn(List.of(entity));

        List<DhoOpportunityDTO> result = service.findAll();

        assertEquals(1, result.size());
        assertEquals("Developer", result.get(0).cargo());
    }

    @Test
    void findByStatus_ShouldReturnFilteredList() {
        DhoOpportunityEntity entity2 = new DhoOpportunityEntity();
        entity2.setStatus("CLOSED");

        when(repository.findAll()).thenReturn(List.of(entity, entity2));

        List<DhoOpportunityDTO> result = service.findByStatus("OPEN");

        assertEquals(1, result.size());
        assertEquals("OPEN", result.get(0).status());
    }

    @Test
    void findByStatus_ShouldReturnEmptyWhenNoMatch() {
        when(repository.findAll()).thenReturn(List.of(entity));

        List<DhoOpportunityDTO> result = service.findByStatus("NON_EXISTENT");

        assertEquals(0, result.size());
    }
}
