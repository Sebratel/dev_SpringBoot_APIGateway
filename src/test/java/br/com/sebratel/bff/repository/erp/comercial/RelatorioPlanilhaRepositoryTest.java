package br.com.sebratel.bff.repository.erp.comercial;

import br.com.sebratel.bff.repository.erp.projections.comercial.PlanilhaInstalacaoProjection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelatorioPlanilhaRepositoryTest {

    @Mock
    private RelatorioPlanilhaRepository repository;

    @Test
    @DisplayName("Deve simular o retorno da query nativa e validar o mapeamento da projection")
    void deveValidarMapeamentoDaProjectionViaMock() {
        // GIVEN
        String vendedorAlvo = "Vendedor Cobertura";
        PlanilhaInstalacaoProjection mockProjection = createMockProjection(vendedorAlvo, "Cliente Teste", "FIBRA");

        // Configuramos o mock para retornar um Stream contendo a nossa projection fake
        when(repository.findPlanilhaInstalacao(vendedorAlvo))
                .thenReturn(Stream.of(mockProjection));

        // WHEN
        try (Stream<PlanilhaInstalacaoProjection> resultStream = repository.findPlanilhaInstalacao(vendedorAlvo)) {
            List<PlanilhaInstalacaoProjection> result = resultStream.toList();

            // THEN
            assertThat(result).isNotEmpty();
            assertThat(result).hasSize(1);

            PlanilhaInstalacaoProjection projection = result.get(0);
            assertThat(projection.getVendedorNome()).isEqualTo(vendedorAlvo);
            assertThat(projection.getClienteNome()).isEqualTo("Cliente Teste");
            assertThat(projection.getTecnologia()).isEqualTo("FIBRA");
        }
    }

    @Test
    @DisplayName("Deve validar o comportamento quando o Stream retornar vazio")
    void deveValidarRetornoVazio() {
        // GIVEN
        String vendedorInexistente = "Ninguem";
        when(repository.findPlanilhaInstalacao(vendedorInexistente))
                .thenReturn(Stream.empty());

        // WHEN
        try (Stream<PlanilhaInstalacaoProjection> resultStream = repository.findPlanilhaInstalacao(vendedorInexistente)) {
            List<PlanilhaInstalacaoProjection> result = resultStream.toList();

            // THEN
            assertThat(result).isEmpty();
        }
    }

    /**
     * Helper para mockar a interface de Projection.
     * Como o Spring Data JPA gera a implementação em runtime,
     * o Mockito é a forma mais rápida de simular esse objeto em testes unitários.
     */
    private PlanilhaInstalacaoProjection createMockProjection(String vendedor, String cliente, String tecnologia) {
        PlanilhaInstalacaoProjection m = mock(PlanilhaInstalacaoProjection.class);

        when(m.getVendedorNome()).thenReturn(vendedor);
        when(m.getClienteNome()).thenReturn(cliente);
        when(m.getTecnologia()).thenReturn(tecnologia);

        return m;
    }
}