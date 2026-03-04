package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.MovimentacaoEstoqueDTO;
import br.com.sebratel.bff.repository.erp.MovimentacaoEstoqueRepository;
import br.com.sebratel.bff.repository.erp.projections.MovimentacaoEstoqueProjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovimentacaoEstoqueServiceTest {

    @Mock
    private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    @InjectMocks
    private MovimentacaoEstoqueService service;

    @Test
    void deveListarEstoqueComMapeamentoCorreto() {
        // GIVEN
        MovimentacaoEstoqueProjection p = mock(MovimentacaoEstoqueProjection.class);
        when(p.getCodigos()).thenReturn("COD-01");
        when(p.getId()).thenReturn(10L);
        when(p.getDescricao()).thenReturn("Produto Teste");
        when(p.getBaseDeOrigem()).thenReturn("Almoxarifado");
        when(p.getEstoqueAtual()).thenReturn(50.0);
        when(p.getMinimo()).thenReturn(10);

        when(movimentacaoEstoqueRepository.findEstoqueMovimentacoes()).thenReturn(List.of(p));

        // WHEN
        List<MovimentacaoEstoqueDTO> resultado = service.listarEstoque();

        // THEN
        assertNotNull(resultado);
        assertEquals(1, resultado.size());

        MovimentacaoEstoqueDTO dto = resultado.get(0);
        assertEquals("COD-01", dto.codigos());
        assertEquals(10L, dto.id());
        assertEquals("Produto Teste", dto.descricao());
        assertEquals("Almoxarifado", dto.baseDeOrigem());
        assertEquals(50, dto.estoqueAtual());
        assertEquals(10, dto.minimo());

        verify(movimentacaoEstoqueRepository, times(1)).findEstoqueMovimentacoes();
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverMovimentacoes() {
        // GIVEN
        when(movimentacaoEstoqueRepository.findEstoqueMovimentacoes()).thenReturn(List.of());

        // WHEN
        List<MovimentacaoEstoqueDTO> resultado = service.listarEstoque();

        // THEN
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(movimentacaoEstoqueRepository, times(1)).findEstoqueMovimentacoes();
    }

    @Test
    void deveMapearCamposNulosCorretamente() {
        // GIVEN
        MovimentacaoEstoqueProjection p = mock(MovimentacaoEstoqueProjection.class);
        when(p.getCodigos()).thenReturn(null);
        when(p.getId()).thenReturn(null);
        when(p.getEstoqueAtual()).thenReturn(null);

        when(movimentacaoEstoqueRepository.findEstoqueMovimentacoes()).thenReturn(List.of(p));

        // WHEN
        List<MovimentacaoEstoqueDTO> resultado = service.listarEstoque();

        // THEN
        assertNotNull(resultado);
        assertNull(resultado.get(0).codigos());
        assertNull(resultado.get(0).id());
        assertNull(resultado.get(0).estoqueAtual());
    }
}