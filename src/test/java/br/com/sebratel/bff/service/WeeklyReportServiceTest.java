package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.RelatorioFinalDTO;
import br.com.sebratel.bff.repository.erp.ContractActivationRepository;
import br.com.sebratel.bff.repository.erp.projections.ContractActivationProjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeeklyReportServiceTest {

    @Mock
    private ContractActivationRepository repository;

    @InjectMocks
    private WeeklyReportService service;

    @Test
    void deveListarDadosCompletosRemovendoDuplicadosEMapeandoMes() {
        ContractActivationProjection p1 = mock(ContractActivationProjection.class);
        when(p1.getClientes()).thenReturn("Cliente A");
        when(p1.getContrato()).thenReturn("100");
        when(p1.getDataCriacaoContrato()).thenReturn(LocalDateTime.of(2024, 1, 15, 10, 0));

        // Duplicado de p1 (mesmo cliente e contrato)
        ContractActivationProjection p2 = mock(ContractActivationProjection.class);
        when(p2.getClientes()).thenReturn("Cliente A");
        when(p2.getContrato()).thenReturn("100");

        // Objeto com data nula para testar condição do mapToDTO
        ContractActivationProjection p3 = mock(ContractActivationProjection.class);
        when(p3.getClientes()).thenReturn("Cliente B");
        when(p3.getContrato()).thenReturn("200");
        when(p3.getDataCriacaoContrato()).thenReturn(null);

        when(repository.findMergedContractData()).thenReturn(List.of(p1, p2, p3));

        List<RelatorioFinalDTO> resultado = service.sellersReportStream("C1").toList();

        assertEquals(2, resultado.size());
        assertEquals("Janeiro", resultado.get(0).mesDaCriacao());
        assertEquals("", resultado.get(1).mesDaCriacao()); // Data nula resulta em string vazia
    }

    @Test
    void deveFiltrarPorVendedorEMesAtualComSucesso() {
        // Fixando a data para Janeiro para o teste ser determinístico
        LocalDate dataFixa = LocalDate.of(2024, 1, 1);
        try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class)) {
            mockedLocalDate.when(LocalDate::now).thenReturn(dataFixa);

            // Criando DTOs mockados que simulariam o retorno do cache
            RelatorioFinalDTO dtoMatch = new RelatorioFinalDTO(
                    null, null, "1", "VENDEDOR TESTE", "C1", "FTTH", "Ativo", null, "Janeiro");

            RelatorioFinalDTO dtoVendedorErrado = new RelatorioFinalDTO(
                    null, null, "2", "OUTRO", "C2", "FTTH", "Ativo", null, "Janeiro");

            RelatorioFinalDTO dtoMesErrado = new RelatorioFinalDTO(
                    null, null, "3", "VENDEDOR TESTE", "C3", "FTTH", "Ativo", null, "Fevereiro");

            // Injetando dados via Mockito para simular a chamada ao método cacheado internamente
            WeeklyReportService serviceSpy = spy(service);
            doReturn(List.of(dtoMatch, dtoVendedorErrado, dtoMesErrado)).when(serviceSpy).sellersReportStream("C1");

            Stream<RelatorioFinalDTO> resultado = serviceSpy.sellersReportStream("  vendedor teste  ");

            List<RelatorioFinalDTO> lista = resultado.toList();
            assertEquals(1, lista.size());
            assertEquals("VENDEDOR TESTE", lista.get(0).vendedor());
            assertEquals("Janeiro", lista.get(0).mesDaCriacao());
        }
    }

    @Test
    void deveRetornarStreamVazioQuandoVendedorForNuloNoFiltro() {
        RelatorioFinalDTO dtoVendedorNulo = new RelatorioFinalDTO(
                null, null, "1", null, "C1", "FTTH", "Ativo", null, "Janeiro");

        WeeklyReportService serviceSpy = spy(service);
        doReturn(List.of(dtoVendedorNulo)).when(serviceSpy).sellersReportStream("C1");

        Stream<RelatorioFinalDTO> resultado = serviceSpy.sellersReportStream("QUALQUER");

        assertTrue(resultado.findAny().isEmpty());
    }

    @Test
    void deveVerificarCapitalizacaoDoMesNoMapToDTO() {
        ContractActivationProjection p = mock(ContractActivationProjection.class);
        when(p.getDataCriacaoContrato()).thenReturn(LocalDateTime.of(2024, 5, 10, 0, 0)); // Maio
        when(p.getClientes()).thenReturn("Cliente");
        when(p.getContrato()).thenReturn("1");

        when(repository.findMergedContractData()).thenReturn(List.of(p));

        List<RelatorioFinalDTO> resultado = service.sellersReportStream("C1").toList();

        // Verifica se "maio" virou "Maio" (Primeira letra maiúscula)
        assertEquals("Maio", resultado.get(0).mesDaCriacao());
    }
}