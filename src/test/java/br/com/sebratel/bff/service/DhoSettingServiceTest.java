package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.DhoSettingDTO;
import br.com.sebratel.bff.model.entity.DhoSettingEntity;
import br.com.sebratel.bff.repository.afetados.DhoSettingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DhoSettingServiceTest {

    @Mock
    private DhoSettingRepository repository;

    @InjectMocks
    private DhoSettingService service;

    @Test
    void findAll_ShouldReturnList() {
        DhoSettingEntity entity = new DhoSettingEntity();
        entity.setCargo("Developer");
        entity.setArea("Tech");

        when(repository.findAll()).thenReturn(List.of(entity));

        List<DhoSettingDTO> result = service.findAll();

        assertEquals(1, result.size());
        assertEquals("Developer", result.get(0).cargo());
    }
}
