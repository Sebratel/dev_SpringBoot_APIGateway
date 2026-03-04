package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.UltimaConexaoDTO;
import br.com.sebratel.bff.repository.radius.UltimaConexaoRepository;
import br.com.sebratel.bff.repository.radius.projections.UltimaConexaoProjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UltimaConexaoServiceTest {

    @Mock
    private UltimaConexaoRepository repository;

    @InjectMocks
    private UltimaConexaoService service;

    @Test
    void deveListarUltimasConexoesComMapeamentoCompleto() {
        // GIVEN
        UltimaConexaoProjection p = mock(UltimaConexaoProjection.class);
        LocalDateTime agora = LocalDateTime.now();

        when(p.getUsuario()).thenReturn("usuario.teste");
        when(p.getInicio()).thenReturn(agora.minusHours(2));
        when(p.getAtualizado()).thenReturn(agora);
        when(p.getPausado()).thenReturn(agora.minusMinutes(5));
        when(p.getRecebendo()).thenReturn(1024L);
        when(p.getEnviando()).thenReturn(512L);
        when(p.getIpConexao()).thenReturn("192.168.1.100");

        when(repository.findUltimasConexoesAtivas()).thenReturn(List.of(p));

        // WHEN
        List<UltimaConexaoDTO> resultado = service.listarUltimasConexoes();

        // THEN
        assertNotNull(resultado);
        assertEquals(1, resultado.size());

        UltimaConexaoDTO dto = resultado.get(0);
        assertEquals("usuario.teste", dto.usuario());
        assertEquals(agora.minusHours(2), dto.inicio());
        assertEquals(agora, dto.atualizado());
        assertEquals(agora.minusMinutes(5), dto.pausado());
        assertEquals(1024L, dto.recebendo());
        assertEquals(512L, dto.enviando());
        assertEquals("192.168.1.100", dto.ipConexao());

        verify(repository, times(1)).findUltimasConexoesAtivas();
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverConexoes() {
        // GIVEN
        when(repository.findUltimasConexoesAtivas()).thenReturn(List.of());

        // WHEN
        List<UltimaConexaoDTO> resultado = service.listarUltimasConexoes();

        // THEN
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(repository, times(1)).findUltimasConexoesAtivas();
    }

    @Test
    void deveMapearCamposNulosCorretamente() {
        // GIVEN
        UltimaConexaoProjection p = mock(UltimaConexaoProjection.class);
        when(p.getUsuario()).thenReturn(null);
        when(p.getIpConexao()).thenReturn(null);
        when(p.getRecebendo()).thenReturn(null);

        when(repository.findUltimasConexoesAtivas()).thenReturn(List.of(p));

        // WHEN
        List<UltimaConexaoDTO> resultado = service.listarUltimasConexoes();

        // THEN
        assertNotNull(resultado);
        assertNull(resultado.get(0).usuario());
        assertNull(resultado.get(0).ipConexao());
        assertNull(resultado.get(0).recebendo());
    }
}