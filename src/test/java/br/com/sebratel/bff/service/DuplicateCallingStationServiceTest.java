package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.DuplicateCallingStationDTO;
import br.com.sebratel.bff.repository.radius.DuplicateCallingStationRepository;
import br.com.sebratel.bff.repository.radius.projections.DuplicateCallingStationProjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DuplicateCallingStationServiceTest {

    @Mock
    private DuplicateCallingStationRepository repository;

    @InjectMocks
    private DuplicateCallingStationService service;

    @Test
    void deveListarConexoesDuplicadasComSucesso() {
        // GIVEN
        DuplicateCallingStationProjection p = mock(DuplicateCallingStationProjection.class);
        when(p.getUsername()).thenReturn("usuario.teste");
        when(p.getUniqueCallingstationCount()).thenReturn(2L);

        when(repository.findDuplicateCallingStations()).thenReturn(List.of(p));

        // WHEN
        List<DuplicateCallingStationDTO> resultado = service.listarConexoesDuplicadas();

        // THEN
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("usuario.teste", resultado.get(0).username());
        assertEquals(2L, resultado.get(0).uniqueCallingstationCount());

        verify(repository, times(1)).findDuplicateCallingStations();
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverDuplicidades() {
        // GIVEN
        when(repository.findDuplicateCallingStations()).thenReturn(List.of());

        // WHEN
        List<DuplicateCallingStationDTO> resultado = service.listarConexoesDuplicadas();

        // THEN
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(repository, times(1)).findDuplicateCallingStations();
    }

    @Test
    void deveMapearCamposNulosCorretamente() {
        // GIVEN
        DuplicateCallingStationProjection p = mock(DuplicateCallingStationProjection.class);
        when(p.getUsername()).thenReturn(null);
        when(p.getUniqueCallingstationCount()).thenReturn(null);

        when(repository.findDuplicateCallingStations()).thenReturn(List.of(p));

        // WHEN
        List<DuplicateCallingStationDTO> resultado = service.listarConexoesDuplicadas();

        // THEN
        assertNotNull(resultado);
        assertNull(resultado.get(0).username());
        assertNull(resultado.get(0).uniqueCallingstationCount());
    }
}