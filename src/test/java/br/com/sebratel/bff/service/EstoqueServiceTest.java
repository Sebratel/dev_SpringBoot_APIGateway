package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.EstoqueTecnicoDTO;
import br.com.sebratel.bff.repository.erp.EstoqueRepository;
import br.com.sebratel.bff.repository.erp.projections.EstoqueProjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstoqueServiceTest {

    @Mock
    private EstoqueRepository repository;

    @InjectMocks
    private EstoqueService service;

    @Test
    void deveBuscarEstoquePorTecnicoComSucesso() {
        EstoqueProjection view = mock(EstoqueProjection.class);
        when(view.getCodigo()).thenReturn("PROD001");
        when(view.getDescricao()).thenReturn("Cabo de Rede");
        when(view.getTecnico()).thenReturn("João Silva");
        when(view.getPossui()).thenReturn(10);
        when(view.getId()).thenReturn(100L);

        when(repository.findEstoqueByTecnicoNative("João Silva")).thenReturn(List.of(view));

        List<EstoqueTecnicoDTO> resultado = service.buscarEstoquePorTecnico("João Silva");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());

        EstoqueTecnicoDTO dto = resultado.get(0);
        assertEquals("PROD001", dto.codigo());
        assertEquals("Cabo de Rede", dto.descricao());
        assertEquals("João Silva", dto.tecnico());
        assertEquals(10L, dto.possui()); // LongValue de 10.5 é 10
        assertEquals(100L, dto.id());

        verify(repository, times(1)).findEstoqueByTecnicoNative("João Silva");
    }

    @Test
    void deveTratarPossuiComoZeroQuandoForNulo() {
        EstoqueProjection view = mock(EstoqueProjection.class);
        when(view.getPossui()).thenReturn(null);

        when(repository.findEstoqueByTecnicoNative(anyString())).thenReturn(List.of(view));

        List<EstoqueTecnicoDTO> resultado = service.buscarEstoquePorTecnico("Técnico");

        assertEquals(0L, resultado.get(0).possui());
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverDados() {
        when(repository.findEstoqueByTecnicoNative(anyString())).thenReturn(List.of());

        List<EstoqueTecnicoDTO> resultado = service.buscarEstoquePorTecnico("Qualquer");

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(repository, times(1)).findEstoqueByTecnicoNative("Qualquer");
    }

    @Test
    void deveMapearCamposNulosCorretamente() {
        EstoqueProjection view = mock(EstoqueProjection.class);
        when(view.getCodigo()).thenReturn(null);
        when(view.getDescricao()).thenReturn(null);
        when(view.getId()).thenReturn(null);

        when(repository.findEstoqueByTecnicoNative(anyString())).thenReturn(List.of(view));

        List<EstoqueTecnicoDTO> resultado = service.buscarEstoquePorTecnico("Técnico");

        assertNotNull(resultado);
        assertNull(resultado.get(0).codigo());
        assertNull(resultado.get(0).descricao());
        assertNull(resultado.get(0).id());
    }
}