package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.VendedoresAtivosDTO;
import br.com.sebratel.bff.repository.erp.VendedoresAtivosRepository;
import br.com.sebratel.bff.repository.erp.projections.VendedoresProjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VendedoresAtivosServiceTest {

    @Mock
    private VendedoresAtivosRepository vendedorRepository;

    @InjectMocks
    private VendedoresAtivosService service;

    @Test
    void deveListarVendedoresAtivosComSucesso() {
        // GIVEN
        // Assumindo que o repositório retorna uma Projeção ou Entity compatível com o construtor do DTO
        VendedoresProjection vendedoresProjection = mock(VendedoresProjection.class);
        when(vendedorRepository.findVendedoresAtivos()).thenReturn(List.of(vendedoresProjection));

        // WHEN
        List<VendedoresAtivosDTO> resultado = service.listarVendedoresAtivos();

        // THEN
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(vendedorRepository, times(1)).findVendedoresAtivos();
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverVendedores() {
        // GIVEN
        when(vendedorRepository.findVendedoresAtivos()).thenReturn(List.of());

        // WHEN
        List<VendedoresAtivosDTO> resultado = service.listarVendedoresAtivos();

        // THEN
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(vendedorRepository, times(1)).findVendedoresAtivos();
    }

    @Test
    void deveVerificarSeChamadaAoRepositorioOcorreUmaVez() {
        // GIVEN
        when(vendedorRepository.findVendedoresAtivos()).thenReturn(List.of());

        // WHEN
        service.listarVendedoresAtivos();

        // THEN
        verify(vendedorRepository, only()).findVendedoresAtivos();
    }
}