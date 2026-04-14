package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.DhoOpportunityDTO;
import br.com.sebratel.bff.model.entity.EmployeeEntity;
import br.com.sebratel.bff.repository.afetados.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DhoOpportunityServiceTest {

    @Mock
    private EmployeeRepository repository;

    @InjectMocks
    private DhoOpportunityService service;

    private EmployeeEntity entity;

    @BeforeEach
    void setUp() {
        entity = new EmployeeEntity();
        entity.setId(1L);
        entity.setRegistration(123);
        entity.setAdmissionDate(LocalDateTime.now());
        entity.setEmail("test@test.com");
        entity.setStatus("ACTIVE");
    }

    @Test
    void findAll_ShouldReturnList() {
        when(repository.findAll()).thenReturn(List.of(entity));

        List<DhoOpportunityDTO> result = service.findAll();

        assertEquals(1, result.size());
        assertEquals("test@test.com", result.get(0).email());
    }

    @Test
    void findByStatus_ShouldReturnFilteredList() {
        EmployeeEntity entity2 = new EmployeeEntity();
        entity2.setStatus("INACTIVE");

        when(repository.findAll()).thenReturn(List.of(entity, entity2));

        List<DhoOpportunityDTO> result = service.findByStatus("ACTIVE");

        assertEquals(1, result.size());
        assertEquals("ACTIVE", result.get(0).status());
    }

    @Test
    void findByStatus_ShouldReturnEmptyWhenNoMatch() {
        when(repository.findAll()).thenReturn(List.of(entity));

        List<DhoOpportunityDTO> result = service.findByStatus("NON_EXISTENT");

        assertEquals(0, result.size());
    }
}
