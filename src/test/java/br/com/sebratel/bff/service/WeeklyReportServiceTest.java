package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.RelatorioFinalDTO;
import org.junit.jupiter.api.DisplayName;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeeklyReportServiceTest {

    @Mock
    private ContractDataService contractDataService;

    @InjectMocks
    private WeeklyReportService weeklyReportService;

    @Test
    @DisplayName("Deve filtrar corretamente por vendedor e mês atual (com capitalização)")
    void deveFiltrarPorVendedorEMesAtualComSucesso() {
        // GIVEN: Fixamos a data em Janeiro
        LocalDate dataFixa = LocalDate.of(2024, 1, 15);

        try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class)) {
            mockedLocalDate.when(LocalDate::now).thenReturn(dataFixa);

            RelatorioFinalDTO dtoMatch = createDTO("VENDEDOR TESTE", "Janeiro");
            RelatorioFinalDTO dtoVendedorErrado = createDTO("OUTRO VENDEDOR", "Janeiro");
            RelatorioFinalDTO dtoMesErrado = createDTO("VENDEDOR TESTE", "Fevereiro");

            when(contractDataService.getDadosCompletosCache())
                    .thenReturn(List.of(dtoMatch, dtoVendedorErrado, dtoMesErrado));

            // WHEN
            List<RelatorioFinalDTO> lista = weeklyReportService.sellersReportStream("  vendedor teste  ").toList();

            // THEN
            assertThat(lista).hasSize(1);
            assertThat(lista.get(0).vendedor()).isEqualTo("VENDEDOR TESTE");
            assertThat(lista.get(0).mesDaCriacao()).isEqualTo("Janeiro");
        }
    }

    @Test
    @DisplayName("Deve retornar vazio quando o vendedor for nulo no cache")
    void deveRetornarVazioQuandoVendedorForNuloNoCache() {
        // O construtor da record que sugeri antes trata null para "",
        // mas aqui testamos o comportamento do filtro caso venha nulo.
        RelatorioFinalDTO dtoVendedorNulo = createDTO(null, "Janeiro");

        when(contractDataService.getDadosCompletosCache()).thenReturn(List.of(dtoVendedorNulo));

        Stream<RelatorioFinalDTO> resultado = weeklyReportService.sellersReportStream("QUALQUER");

        assertThat(resultado.toList()).isEmpty();
    }

    @Test
    @DisplayName("Deve respeitar a capitalização do mês ao filtrar (ex: Maio)")
    void deveRespeitarCapitalizacaoDoMes() {
        // GIVEN: Fixamos em Maio
        LocalDate dataMaio = LocalDate.of(2024, 5, 1);

        try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class)) {
            mockedLocalDate.when(LocalDate::now).thenReturn(dataMaio);

            RelatorioFinalDTO dtoMaio = createDTO("VENDEDOR", "Maio");
            when(contractDataService.getDadosCompletosCache()).thenReturn(List.of(dtoMaio));

            // WHEN
            List<RelatorioFinalDTO> resultado = weeklyReportService.sellersReportStream("VENDEDOR").toList();

            // THEN
            assertThat(resultado).isNotEmpty();
            assertThat(resultado.get(0).mesDaCriacao()).isEqualTo("Maio");
        }
    }

    /**
     * Ajustado para bater com os campos da sua Record:
     * dataCriacaoContrato, dataAtivacao, contrato, vendedor, clientes, tecnologia, statusContrato, statusCancelamento, mesDaCriacao
     */
    private RelatorioFinalDTO createDTO(String vendedor, String mes) {
        return new RelatorioFinalDTO(
                LocalDateTime.now(), // dataCriacaoContrato
                LocalDateTime.now(), // dataAtivacao
                "CTR-123",           // contrato
                vendedor,            // vendedor
                "Cliente Teste",     // clientes
                "FTTH",              // tecnologia
                "Ativo",             // statusContrato
                null,                // statusCancelamento
                mes                  // mesDaCriacao
        );
    }
}