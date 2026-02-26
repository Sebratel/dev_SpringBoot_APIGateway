package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.AquisicaoDTO;
import br.com.sebratel.bff.repository.erp.AquisicaoRepository;
import br.com.sebratel.bff.repository.erp.projections.AquisicaoProjection;
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
class AquisicaoServiceTest {

    @Mock
    private AquisicaoRepository repository;

    @InjectMocks
    private AquisicaoService service;

    @Test
    void deveListarAquisicoesComMapeamentoCompleto() {
        AquisicaoProjection p = mock(AquisicaoProjection.class);
        LocalDate agora = LocalDate.now();

        when(p.getId()).thenReturn(1L);
        when(p.getCodigo()).thenReturn("COD123");
        when(p.getProduto()).thenReturn("Produto Teste");
        when(p.getData()).thenReturn(agora);
        when(p.getRequisitadoPor()).thenReturn("Usuario Teste");
        when(p.getDataPrevisao()).thenReturn(agora.plusDays(5));
        when(p.getOutrosStatus()).thenReturn("");
        when(p.getBase()).thenReturn("Base Central");
        when(p.getStatus()).thenReturn("ATIVO");

        when(repository.findAquisicoesPendentes()).thenReturn(List.of(p));

        List<AquisicaoDTO> resultado = service.listarAquisicoes();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());

        AquisicaoDTO dto = resultado.get(0);
        assertEquals(1L, dto.id());
        assertEquals("COD123", dto.codigo());
        assertEquals("Produto Teste", dto.produto());
        assertEquals(agora, dto.data());
        assertEquals("Usuario Teste", dto.requisitadoPor());
        assertEquals(agora.plusDays(5), dto.dataPrevisao());
        assertEquals("", dto.outroStatus());
        assertEquals("Base Central", dto.base());
        assertEquals("ATIVO", dto.status());

        verify(repository, times(1)).findAquisicoesPendentes();
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverAquisicoes() {
        when(repository.findAquisicoesPendentes()).thenReturn(List.of());

        List<AquisicaoDTO> resultado = service.listarAquisicoes();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(repository, times(1)).findAquisicoesPendentes();
    }

    @Test
    void deveMapearCamposNulosCorretamente() {
        AquisicaoProjection p = mock(AquisicaoProjection.class);
        when(p.getId()).thenReturn(null);
        when(p.getProduto()).thenReturn(null);

        when(repository.findAquisicoesPendentes()).thenReturn(List.of(p));

        List<AquisicaoDTO> resultado = service.listarAquisicoes();

        assertNull(resultado.get(0).id());
        assertNull(resultado.get(0).produto());
    }
}