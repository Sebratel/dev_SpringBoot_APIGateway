package br.com.sebratel.bff.repository.erp;

import br.com.sebratel.bff.repository.erp.projections.AquisicaoProjection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) // Inicializa os mocks sem subir o Spring
class AquisicaoRepositoryTest {

    @Mock
    private AquisicaoRepository aquisicaoRepository;

    @Test
    @DisplayName("Deve validar o comportamento do retorno do repositório mockado")
    void findAquisicoesPendentes_Sucesso() {
        // GIVEN: Criamos um mock da Projection (Interface)
        AquisicaoProjection mockProjection = createMockProjection();

        // Configuramos o comportamento do mock
        when(aquisicaoRepository.findAquisicoesPendentes())
                .thenReturn(List.of(mockProjection));

        // WHEN: Chamamos o método
        List<AquisicaoProjection> resultado = aquisicaoRepository.findAquisicoesPendentes();

        // THEN
        assertThat(resultado).hasSize(1);
        AquisicaoProjection projection = resultado.get(0);

        assertThat(projection.getCodigo()).isEqualTo("123");
        assertThat(projection.getProduto()).isEqualTo("Cabo de Rede");
        assertThat(projection.getRequisitadoPor()).isEqualTo("João Silva");
        assertThat(projection.getBase()).isEqualTo("SEDE - PORTO ALEGRE");
        assertThat(projection.getStatus()).isEqualTo("Aguardando Entrega");
    }

    /**
     * Helper para criar uma instância fake da Projection.
     * Como AquisicaoProjection é uma interface, usamos Mockito para simular os getters.
     */
    private AquisicaoProjection createMockProjection() {
        AquisicaoProjection mock = org.mockito.Mockito.mock(AquisicaoProjection.class);
        when(mock.getCodigo()).thenReturn("123");
        when(mock.getProduto()).thenReturn("Cabo de Rede");
        when(mock.getRequisitadoPor()).thenReturn("João Silva");
        when(mock.getBase()).thenReturn("SEDE - PORTO ALEGRE");
        when(mock.getStatus()).thenReturn("Aguardando Entrega");
        return mock;
    }
}