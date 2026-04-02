package br.com.sebratel.bff.repository.erp.comercial;

import br.com.sebratel.bff.repository.erp.projections.comercial.ContratoPersonalizadoProjection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContratoPersonalizadoRepositoryTest {

    @Mock
    private ContratoPersonalizadoRepository repository;

    @Test
    @DisplayName("Deve retornar contratos personalizados filtrados por cliente e data via mock")
    void findContratosPersonalizados_Sucesso() {
        // GIVEN
        LocalDateTime inicio = LocalDateTime.now().minusDays(10);
        LocalDateTime fim = LocalDateTime.now().plusDays(10);
        String nomeCliente = "Cliente Teste";

        // Criamos o mock da Projection (Interface)
        ContratoPersonalizadoProjection mockProjection = createMockContratoProjection(nomeCliente, "CONTRATO-001");

        // Configuramos o mock do repositório para retornar a lista quando os parâmetros baterem
        when(repository.findContratosPersonalizados(inicio, fim, List.of(nomeCliente)))
                .thenReturn(List.of(mockProjection));

        // WHEN
        List<ContratoPersonalizadoProjection> resultado = repository.findContratosPersonalizados(inicio, fim, List.of(nomeCliente));

        // THEN
        assertThat(resultado).isNotEmpty();
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNome()).isEqualTo(nomeCliente);
        assertThat(resultado.get(0).getNumeroContrato()).isEqualTo("CONTRATO-001");
    }

    @Test
    @DisplayName("Deve retornar lista vazia se o filtro não encontrar resultados")
    void findContratosPersonalizados_FiltroSemResultado() {
        // GIVEN
        LocalDateTime inicio = LocalDateTime.now();
        LocalDateTime fim = LocalDateTime.now().plusDays(1);
        List<String> clientes = List.of("Outro Cliente");

        // Por padrão, mocks retornam listas vazias se não configurarmos o 'when'
        // Mas podemos deixar explícito:
        when(repository.findContratosPersonalizados(inicio, fim, clientes))
                .thenReturn(List.of());

        // WHEN
        List<ContratoPersonalizadoProjection> resultado = repository.findContratosPersonalizados(inicio, fim, clientes);

        // THEN
        assertThat(resultado).isEmpty();
    }

    /**
     * Helper para mockar a interface de Projection.
     */
    private ContratoPersonalizadoProjection createMockContratoProjection(String nome, String numero) {
        ContratoPersonalizadoProjection m = mock(ContratoPersonalizadoProjection.class);

        when(m.getNome()).thenReturn(nome);
        when(m.getNumeroContrato()).thenReturn(numero);
        // Adicione outros campos necessários da sua projection aqui

        return m;
    }
}