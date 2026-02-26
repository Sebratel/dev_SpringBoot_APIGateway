package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.ContratoAtivacaoFaturaDTO;
import br.com.sebratel.bff.repository.erp.ContratoAtivacaoFaturaRepository;
import br.com.sebratel.bff.repository.erp.projections.ContratoAtivacaoFaturaProjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContratoAtivacaoFaturaServiceTest {

    @Mock
    private ContratoAtivacaoFaturaRepository repository;

    @InjectMocks
    private ContratoAtivacaoFaturaService service;

    @Test
    void deveListarContratosRelacionadosComSucesso() {
        ContratoAtivacaoFaturaProjection p = mock(ContratoAtivacaoFaturaProjection.class);
        LocalDate agora = LocalDate.now();

        when(p.getContrato()).thenReturn("1001");
        when(p.getVendedor()).thenReturn("Vendedor Teste");
        when(p.getNome()).thenReturn("Cliente Teste");
        when(p.getDataAtivacao()).thenReturn(agora.atStartOfDay());
        when(p.getVencimento()).thenReturn(agora.plusMonths(1));

        when(repository.findContratosAtivacaoFatura()).thenReturn(List.of(p));

        List<ContratoAtivacaoFaturaDTO> resultado = service.listarContratosRelacionados();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());

        ContratoAtivacaoFaturaDTO dto = resultado.get(0);
        assertEquals("1001", dto.contrato());
        assertEquals("Vendedor Teste", dto.vendedor());
        assertEquals("Cliente Teste", dto.nome());
        assertEquals(agora, dto.dataAtivacao().toLocalDate());
        assertEquals(agora.plusMonths(1), dto.vencimento());

        verify(repository, times(1)).findContratosAtivacaoFatura();
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverDados() {
        when(repository.findContratosAtivacaoFatura()).thenReturn(List.of());

        List<ContratoAtivacaoFaturaDTO> resultado = service.listarContratosRelacionados();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(repository, times(1)).findContratosAtivacaoFatura();
    }

    @Test
    void deveMapearCamposNulosCorretamente() {
        ContratoAtivacaoFaturaProjection p = mock(ContratoAtivacaoFaturaProjection.class);
        when(p.getContrato()).thenReturn(null);
        when(p.getVendedor()).thenReturn(null);

        when(repository.findContratosAtivacaoFatura()).thenReturn(List.of(p));

        List<ContratoAtivacaoFaturaDTO> resultado = service.listarContratosRelacionados();

        assertNotNull(resultado);
        assertNull(resultado.get(0).contrato());
        assertNull(resultado.get(0).vendedor());
    }
}