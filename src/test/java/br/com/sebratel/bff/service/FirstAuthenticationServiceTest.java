package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.FirstAuthenticationDTO;
import br.com.sebratel.bff.repository.radius.FirstAuthenticationRepository;
import br.com.sebratel.bff.repository.radius.projections.FirstAuthenticationProjection;
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
class FirstAuthenticationServiceTest {

    @Mock
    private FirstAuthenticationRepository repository;

    @InjectMocks
    private FirstAuthenticationService service;

    @Test
    void deveListarPrimeirasAutenticacoesComSucesso() {
        // GIVEN
        FirstAuthenticationProjection p = mock(FirstAuthenticationProjection.class);
        LocalDateTime dataAutenticacao = LocalDateTime.of(2024, 1, 1, 10, 30);

        when(p.getPppoe()).thenReturn("usuario.teste");
        when(p.getAuthentication()).thenReturn(dataAutenticacao);

        when(repository.findFirstAuthentications()).thenReturn(List.of(p));

        // WHEN
        List<FirstAuthenticationDTO> resultado = service.listarPrimeirasAutenticacoes();

        // THEN
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("usuario.teste", resultado.get(0).pppoe());
        assertEquals(dataAutenticacao, resultado.get(0).authentication());

        verify(repository, times(1)).findFirstAuthentications();
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverDados() {
        // GIVEN
        when(repository.findFirstAuthentications()).thenReturn(List.of());

        // WHEN
        List<FirstAuthenticationDTO> resultado = service.listarPrimeirasAutenticacoes();

        // THEN
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(repository, times(1)).findFirstAuthentications();
    }

    @Test
    void deveMapearCamposNulosCorretamente() {
        // GIVEN
        FirstAuthenticationProjection p = mock(FirstAuthenticationProjection.class);
        when(p.getPppoe()).thenReturn(null);
        when(p.getAuthentication()).thenReturn(null);

        when(repository.findFirstAuthentications()).thenReturn(List.of(p));

        // WHEN
        List<FirstAuthenticationDTO> resultado = service.listarPrimeirasAutenticacoes();

        // THEN
        assertNotNull(resultado);
        assertNull(resultado.get(0).pppoe());
        assertNull(resultado.get(0).authentication());
    }
}