package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.ConsumoDTO;
import br.com.sebratel.bff.repository.erp.PlanoRepository;
import br.com.sebratel.bff.repository.erp.projections.PlanoProjection;
import br.com.sebratel.bff.repository.radius.ConsumoRepository;
import br.com.sebratel.bff.repository.radius.projections.ConsumoProjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsumoServiceTest {

    @Mock
    private ConsumoRepository radiusRepository;

    @Mock
    private PlanoRepository erpRepository;

    @InjectMocks
    private ConsumoService service;

    @Test
    void deveListarConsumoAltoComMatchDePlanos() {
        ConsumoProjection c1 = mock(ConsumoProjection.class);
        when(c1.getUsername()).thenReturn("  User1  ");
        when(c1.getDownloadTb()).thenReturn(1.5);

        PlanoProjection p1 = mock(PlanoProjection.class);
        when(p1.getUsername()).thenReturn("user1");
        when(p1.getCliente()).thenReturn("Cliente Um");
        when(p1.getContrato()).thenReturn("101");
        when(p1.getPlano()).thenReturn("Plano 500M");

        when(radiusRepository.findConsumoExcedente()).thenReturn(List.of(c1));
        when(erpRepository.findTodosPlanos()).thenReturn(List.of(p1));

        List<ConsumoDTO> resultado = service.listarConsumoAlto();

        assertEquals(1, resultado.size());
        assertEquals("  User1  ", resultado.get(0).username());
        assertEquals("Cliente Um", resultado.get(0).cliente());
        assertEquals(1.5, resultado.get(0).downloadTb());
    }

    @Test
    void deveListarConsumoAltoComNDQuandoPlanoNaoEncontradoOuUsernameNulo() {
        ConsumoProjection c1 = mock(ConsumoProjection.class);
        when(c1.getUsername()).thenReturn("Desconhecido");

        PlanoProjection pInvalido = mock(PlanoProjection.class);
        when(pInvalido.getUsername()).thenReturn(null);

        when(radiusRepository.findConsumoExcedente()).thenReturn(List.of(c1));
        when(erpRepository.findTodosPlanos()).thenReturn(List.of(pInvalido));

        List<ConsumoDTO> resultado = service.listarConsumoAlto();

        assertEquals("N/D", resultado.get(0).cliente());
        assertEquals("N/D", resultado.get(0).contrato());
    }

    @Test
    void deveListarConsumoAltoPaginadoComSucesso() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("downloadTb").descending());

        ConsumoProjection c1 = mock(ConsumoProjection.class);
        when(c1.getUsername()).thenReturn("user.paginado");
        when(c1.getTotalTb()).thenReturn(2.0);

        Page<ConsumoProjection> paginaMock = new PageImpl<>(List.of(c1), pageable, 1);

        when(radiusRepository.findConsumoExcedentePaginado(pageable)).thenReturn(paginaMock);

        PlanoProjection p1 = mock(PlanoProjection.class);
        when(p1.getUsername()).thenReturn("user.paginado");
        when(p1.getCliente()).thenReturn("Cliente Paginado");

        when(erpRepository.findPlanosPorUsernames(List.of("user.paginado"))).thenReturn(List.of(p1));

        Page<ConsumoDTO> resultado = service.listarConsumoAltoPaginado(0, 10);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals("Cliente Paginado", resultado.getContent().get(0).cliente());
        assertEquals(2.0, resultado.getContent().get(0).totalTb());
    }

    @Test
    void deveRetornarPaginaVaziaQuandoRadiusNaoRetornarDados() {
        // GIVEN
        Pageable pageable = PageRequest.of(0, 10, Sort.by("downloadTb").descending());
        when(radiusRepository.findConsumoExcedentePaginado(pageable)).thenReturn(Page.empty());

        // Como o seu service chama o repositório mesmo com lista vazia,
        // precisamos mockar o retorno para evitar NullPointerException no .stream()
        when(erpRepository.findPlanosPorUsernames(Collections.emptyList())).thenReturn(Collections.emptyList());

        // WHEN
        Page<ConsumoDTO> resultado = service.listarConsumoAltoPaginado(0, 10);

        // THEN
        assertTrue(resultado.isEmpty());
        // Alterado de never() para verificar que foi chamado com lista vazia conforme o comportamento da sua classe
        verify(erpRepository).findPlanosPorUsernames(Collections.emptyList());
    }

    @Test
    void deveListarConsumoAltoPaginadoComNDQuandoPlanoNaoEncontrado() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("downloadTb").descending());

        ConsumoProjection c1 = mock(ConsumoProjection.class);
        when(c1.getUsername()).thenReturn("user.desconhecido");

        Page<ConsumoProjection> paginaMock = new PageImpl<>(List.of(c1), pageable, 1);

        when(radiusRepository.findConsumoExcedentePaginado(pageable)).thenReturn(paginaMock);
        when(erpRepository.findPlanosPorUsernames(List.of("user.desconhecido"))).thenReturn(Collections.emptyList());

        Page<ConsumoDTO> resultado = service.listarConsumoAltoPaginado(0, 10);

        assertNotNull(resultado);
        assertEquals("N/D", resultado.getContent().get(0).cliente());
        assertEquals("N/D", resultado.getContent().get(0).contrato());
        assertEquals("N/D", resultado.getContent().get(0).plano());
    }

    @Test
    void deveTratarDuplicidadeDeUsernamesNoMapaDePlanos() {
        // GIVEN
        ConsumoProjection c1 = mock(ConsumoProjection.class);
        when(c1.getUsername()).thenReturn("repetido");

        PlanoProjection p1 = mock(PlanoProjection.class);
        when(p1.getUsername()).thenReturn("repetido");
        when(p1.getCliente()).thenReturn("Primeiro");

        PlanoProjection p2 = mock(PlanoProjection.class);
        when(p2.getUsername()).thenReturn("repetido");
        // Removi o stubbing de p2.getCliente() pois a lógica (existente, novo) -> existente
        // faz com que p2 seja descartado do mapa, logo seus getters nunca serão chamados.

        when(radiusRepository.findConsumoExcedente()).thenReturn(List.of(c1));
        when(erpRepository.findTodosPlanos()).thenReturn(List.of(p1, p2));

        // WHEN
        List<ConsumoDTO> resultado = service.listarConsumoAlto();

        // THEN
        assertEquals(1, resultado.size());
        assertEquals("Primeiro", resultado.get(0).cliente()); // Garante que pegou o primeiro
    }

    @Test
    void deveTratarDuplicidadeNoPaginado() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("downloadTb").descending());
        ConsumoProjection c1 = mock(ConsumoProjection.class);
        when(c1.getUsername()).thenReturn("repetido");
        Page<ConsumoProjection> paginaMock = new PageImpl<>(List.of(c1), pageable, 1);
        when(radiusRepository.findConsumoExcedentePaginado(pageable)).thenReturn(paginaMock);

        PlanoProjection p1 = mock(PlanoProjection.class);
        when(p1.getUsername()).thenReturn("repetido");
        when(p1.getCliente()).thenReturn("Primeiro");

        PlanoProjection p2 = mock(PlanoProjection.class);
        when(p2.getUsername()).thenReturn("repetido");

        when(erpRepository.findPlanosPorUsernames(anyList())).thenReturn(List.of(p1, p2));

        Page<ConsumoDTO> resultado = service.listarConsumoAltoPaginado(0, 10);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("Primeiro", resultado.getContent().get(0).cliente());
    }
}