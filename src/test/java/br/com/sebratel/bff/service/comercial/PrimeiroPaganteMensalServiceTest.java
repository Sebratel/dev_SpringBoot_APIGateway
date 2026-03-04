package br.com.sebratel.bff.service.comercial;

import br.com.sebratel.bff.dto.VendedoresAtivosDTO;
import br.com.sebratel.bff.dto.comercial.ContratoPersonalizadoDTO;
import br.com.sebratel.bff.dto.comercial.PlanilhaInstalacaoDTO;
import br.com.sebratel.bff.dto.comercial.RelatorioPorVendedorDTO;
import br.com.sebratel.bff.service.VendedoresAtivosService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrimeiroPaganteMensalServiceTest {

    @Mock
    private ContratoPersonalizadoService personalizadoService;

    @Mock
    private RelatorioPlanilhaService planilhaService;

    @Mock
    private VendedoresAtivosService vendedoresAtivosService;

    @InjectMocks
    private PrimeiroPaganteMensalService service;

    @Test
    void deveProcessarFiltroELoopComSucesso() {
        VendedoresAtivosDTO vendedor = new VendedoresAtivosDTO("Vendedor Teste", "vendedor@teste.com");
        when(vendedoresAtivosService.listarVendedoresAtivos()).thenReturn(List.of(vendedor));

        LocalDateTime dataComum = LocalDateTime.now().minusMonths(1).withDayOfMonth(1);

        PlanilhaInstalacaoDTO planilha = new PlanilhaInstalacaoDTO();
        planilha.setClienteNome("CLIENTE 1");
        planilha.setDataCriacaoContrato(dataComum);
        planilha.setVendedorNome("VENDEDOR TESTE");
        planilha.setDataSaida(LocalDate.now().atStartOfDay());

        when(planilhaService.listarPlanilhaInstalacao("VENDEDOR TESTE"))
                .thenReturn(Stream.of(planilha));

        ContratoPersonalizadoDTO contrato = new ContratoPersonalizadoDTO();
        contrato.setNome("CLIENTE 1");
        contrato.setDataCriacao(dataComum);
        contrato.setNumeroContrato("123");

        when(personalizadoService.listarContratosPersonalizados(any(), any(), anyList()))
                .thenReturn(Stream.of(contrato));

        List<RelatorioPorVendedorDTO> resultado = service.filtroELoop();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("VENDEDOR TESTE", resultado.get(0).getDadosDoVendedor().nome());
        assertFalse(resultado.get(0).getRelatorioPorVendedor().isEmpty());
        assertEquals("CLIENTE 1", resultado.get(0).getRelatorioPorVendedor().get(0).getNome());
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverVendedores() {
        when(vendedoresAtivosService.listarVendedoresAtivos()).thenReturn(List.of());

        List<RelatorioPorVendedorDTO> resultado = service.filtroELoop();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveRetornarRelatorioVazioSeNaoHouverMatchDeContratos() {
        VendedoresAtivosDTO vendedor = new VendedoresAtivosDTO("Vendedor Sem Match", "vendedor@teste.com");
        when(vendedoresAtivosService.listarVendedoresAtivos()).thenReturn(List.of(vendedor));

        PlanilhaInstalacaoDTO planilha = new PlanilhaInstalacaoDTO();
        planilha.setClienteNome("CLIENTE A");
        planilha.setDataCriacaoContrato(LocalDateTime.now());

        when(planilhaService.listarPlanilhaInstalacao(anyString()))
                .thenReturn(Stream.of(planilha));

        when(personalizadoService.listarContratosPersonalizados(any(), any(), anyList()))
                .thenReturn(Stream.empty());

        List<RelatorioPorVendedorDTO> resultado = service.filtroELoop();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getRelatorioPorVendedor().isEmpty());
    }

    @Test
    void deveTratarNomeVendedorNuloSemErro() {
        VendedoresAtivosDTO vendedorNull = new VendedoresAtivosDTO(null, "email@teste.com");
        when(vendedoresAtivosService.listarVendedoresAtivos()).thenReturn(List.of(vendedorNull));

        when(planilhaService.listarPlanilhaInstalacao(null)).thenReturn(Stream.empty());

        List<RelatorioPorVendedorDTO> resultado = service.filtroELoop();

        assertNotNull(resultado);
        assertNull(resultado.get(0).getDadosDoVendedor().nome());
    }

    @Test
    void deveTratarDataRetornoNulaNoRelatorio() {
        VendedoresAtivosDTO vendedor = new VendedoresAtivosDTO("VENDEDOR", "email@teste.com");
        when(vendedoresAtivosService.listarVendedoresAtivos()).thenReturn(List.of(vendedor));

        LocalDateTime dataComum = LocalDateTime.now().minusMonths(1).withDayOfMonth(1);

        PlanilhaInstalacaoDTO planilha = new PlanilhaInstalacaoDTO();
        planilha.setClienteNome("CLIENTE 1");
        planilha.setDataCriacaoContrato(dataComum);
        planilha.setVendedorNome("VENDEDOR");
        planilha.setDataRetorno(null);

        when(planilhaService.listarPlanilhaInstalacao("VENDEDOR")).thenReturn(Stream.of(planilha));

        ContratoPersonalizadoDTO contrato = new ContratoPersonalizadoDTO();
        contrato.setNome("CLIENTE 1");
        contrato.setDataCriacao(dataComum);
        contrato.setNumeroContrato("123");

        when(personalizadoService.listarContratosPersonalizados(any(), any(), anyList()))
                .thenReturn(Stream.of(contrato));

        List<RelatorioPorVendedorDTO> resultado = service.filtroELoop();

        assertNull(resultado.get(0).getRelatorioPorVendedor().get(0).getDataRetorno());
    }

    @Test
    void deveMapearDataRetornoComoNulaQuandoPlanilhaNaoTiverDataRetorno() {
        // GIVEN
        VendedoresAtivosDTO vendedor = new VendedoresAtivosDTO("VENDEDOR", "v@v.com");
        when(vendedoresAtivosService.listarVendedoresAtivos()).thenReturn(List.of(vendedor));

        LocalDateTime dataContrato = LocalDateTime.now().minusMonths(1).withDayOfMonth(10).withHour(10).withMinute(0);

        PlanilhaInstalacaoDTO planilha = new PlanilhaInstalacaoDTO();
        planilha.setClienteNome("CLIENTE TESTE");
        planilha.setDataCriacaoContrato(dataContrato);
        planilha.setDataRetorno(null); // Cenário dataRetorno == null

        when(planilhaService.listarPlanilhaInstalacao("VENDEDOR")).thenReturn(Stream.of(planilha));

        ContratoPersonalizadoDTO contrato = new ContratoPersonalizadoDTO();
        contrato.setNome("CLIENTE TESTE");
        contrato.setDataCriacao(dataContrato);

        when(personalizadoService.listarContratosPersonalizados(any(), any(), anyList()))
                .thenReturn(Stream.of(contrato));

        // WHEN
        List<RelatorioPorVendedorDTO> resultado = service.filtroELoop();

        // THEN
        assertNull(resultado.get(0).getRelatorioPorVendedor().get(0).getDataRetorno());
    }

    @Test
    void deveMapearDataRetornoParaStartOfDayQuandoPlanilhaTiverDataRetorno() {
        // GIVEN
        VendedoresAtivosDTO vendedor = new VendedoresAtivosDTO("VENDEDOR", "v@v.com");
        when(vendedoresAtivosService.listarVendedoresAtivos()).thenReturn(List.of(vendedor));

        LocalDateTime dataContrato = LocalDateTime.now().minusMonths(1).withDayOfMonth(10).withHour(10).withMinute(0);
        LocalDate dataRetornoSimples = LocalDate.of(2023, 10, 20);

        PlanilhaInstalacaoDTO planilha = new PlanilhaInstalacaoDTO();
        planilha.setClienteNome("CLIENTE TESTE");
        planilha.setDataCriacaoContrato(dataContrato);
        planilha.setDataRetorno(dataRetornoSimples); // Cenário dataRetorno != null

        when(planilhaService.listarPlanilhaInstalacao("VENDEDOR")).thenReturn(Stream.of(planilha));

        ContratoPersonalizadoDTO contrato = new ContratoPersonalizadoDTO();
        contrato.setNome("CLIENTE TESTE");
        contrato.setDataCriacao(dataContrato);

        when(personalizadoService.listarContratosPersonalizados(any(), any(), anyList()))
                .thenReturn(Stream.of(contrato));

        // WHEN
        List<RelatorioPorVendedorDTO> resultado = service.filtroELoop();

        // THEN
        LocalDateTime dataEsperada = dataRetornoSimples.atStartOfDay();
        assertEquals(dataEsperada, resultado.get(0).getRelatorioPorVendedor().get(0).getDataRetorno());
        assertEquals(0, resultado.get(0).getRelatorioPorVendedor().get(0).getDataRetorno().getHour());
    }
}