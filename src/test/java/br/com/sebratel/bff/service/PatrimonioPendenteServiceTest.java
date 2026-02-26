package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.PatrimonioPendenteDTO;
import br.com.sebratel.bff.repository.erp.PatrimonioPendenteRepository;
import br.com.sebratel.bff.repository.erp.projections.PatrimonioPendenteProjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatrimonioPendenteServiceTest {

    @Mock
    private PatrimonioPendenteRepository repository;

    @InjectMocks
    private PatrimonioPendenteService service;

    @Test
    void deveListarPatrimoniosPendentesComSucesso() {
        // GIVEN
        PatrimonioPendenteProjection p = mock(PatrimonioPendenteProjection.class);
        when(p.getCodigo()).thenReturn("PAT-001");
        when(p.getCompanyPlace()).thenReturn("Filial Norte");
        when(p.getProductName()).thenReturn("Roteador TP-Link");
        when(p.getUnidadesPendentes()).thenReturn(5.0);

        when(repository.findPatrimoniosPendentes()).thenReturn(List.of(p));

        // WHEN
        List<PatrimonioPendenteDTO> resultado = service.listarPatrimoniosPendentes();

        // THEN
        assertNotNull(resultado);
        assertEquals(1, resultado.size());

        PatrimonioPendenteDTO dto = resultado.get(0);
        assertEquals("PAT-001", dto.codigo());
        assertEquals("Filial Norte", dto.companyPlace());
        assertEquals("Roteador TP-Link", dto.productName());
        assertEquals(5.0, dto.unidadesPendentes());

        verify(repository, times(1)).findPatrimoniosPendentes();
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverPatrimonios() {
        // GIVEN
        when(repository.findPatrimoniosPendentes()).thenReturn(List.of());

        // WHEN
        List<PatrimonioPendenteDTO> resultado = service.listarPatrimoniosPendentes();

        // THEN
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(repository, times(1)).findPatrimoniosPendentes();
    }

    @Test
    void deveMapearCamposNulosCorretamente() {
        // GIVEN
        PatrimonioPendenteProjection p = mock(PatrimonioPendenteProjection.class);
        when(p.getCodigo()).thenReturn(null);
        when(p.getUnidadesPendentes()).thenReturn(null);

        when(repository.findPatrimoniosPendentes()).thenReturn(List.of(p));

        // WHEN
        List<PatrimonioPendenteDTO> resultado = service.listarPatrimoniosPendentes();

        // THEN
        assertNotNull(resultado);
        assertNull(resultado.get(0).codigo());
        assertNull(resultado.get(0).unidadesPendentes());
    }
}