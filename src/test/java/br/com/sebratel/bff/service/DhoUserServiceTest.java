package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.DhoUserDTO;
import br.com.sebratel.bff.model.entity.DhoUserEntity;
import br.com.sebratel.bff.repository.afetados.DhoUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DhoUserServiceTest {

    @Mock
    private DhoUserRepository repository;

    @InjectMocks
    private DhoUserService service;

    @Test
    void findAll_ShouldReturnList() {
        DhoUserEntity entity = new DhoUserEntity();
        entity.setId(1);
        entity.setName("John Doe");
        entity.setEmail("john@example.com");
        entity.setAccessLevel("ADMIN");

        when(repository.findAll()).thenReturn(List.of(entity));

        List<DhoUserDTO> result = service.findAll();

        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).name());
    }
}
