package br.com.sebratel.bff.service.comercial;

import br.com.sebratel.bff.dto.comercial.ContratoPersonalizadoDTO;
import br.com.sebratel.bff.repository.erp.comercial.ContratoPersonalizadoRepository;
import br.com.sebratel.bff.repository.erp.projections.comercial.ContratoPersonalizadoProjection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContratoPersonalizadoServiceTest {

    @Mock
    private ContratoPersonalizadoRepository repository;

    @InjectMocks
    private ContratoPersonalizadoService service;

    @Test
    @DisplayName("Deve retornar um Stream de DTOs quando houver dados no repositório")
    void deveListarContratosPersonalizadosComSucesso() {
        // GIVEN (Cenário)
        LocalDateTime inicio = LocalDateTime.now().minusDays(10);
        LocalDateTime fim = LocalDateTime.now();
        List<String> clientes = List.of("Cliente A", "Cliente B");

        // Simulando o retorno do repositório.
        // Nota: Assumi que o repository retorna uma lista de objetos compatíveis com o construtor do DTO.
        // Se o repository retornar Entities, substitua 'Object' pela sua Classe Entity.
        ContratoPersonalizadoProjection mockEntity1 = mock(ContratoPersonalizadoProjection.class);
        ContratoPersonalizadoProjection mockEntity2 = mock(ContratoPersonalizadoProjection.class);
        List<ContratoPersonalizadoProjection> lista = List.of(mockEntity1,mockEntity2);
        when(repository.findContratosPersonalizados(inicio, fim, clientes))
                .thenReturn(lista);

        // WHEN (Ação)
        Stream<ContratoPersonalizadoDTO> resultado = service.listarContratosPersonalizados(inicio, fim, clientes);

        // THEN (Validação)
        List<ContratoPersonalizadoDTO> listaResultado = resultado.collect(Collectors.toList());

        assertNotNull(listaResultado);
        assertEquals(2, listaResultado.size());

        // Verifica se o repositório foi chamado exatamente uma vez com os parâmetros corretos
        verify(repository, times(1)).findContratosPersonalizados(inicio, fim, clientes);
    }

    @Test
    @DisplayName("Deve retornar um Stream vazio quando o repositório não encontrar registros")
    void deveRetornarStreamVazioQuandoNaoHouverDados() {
        // GIVEN
        LocalDateTime inicio = LocalDateTime.now();
        LocalDateTime fim = LocalDateTime.now();
        List<String> clientes = List.of("Inexistente");

        when(repository.findContratosPersonalizados(any(), any(), anyList()))
                .thenReturn(List.of());

        // WHEN
        Stream<ContratoPersonalizadoDTO> resultado = service.listarContratosPersonalizados(inicio, fim, clientes);

        // THEN
        assertEquals(0, resultado.count());
        verify(repository, times(1)).findContratosPersonalizados(inicio, fim, clientes);
    }
}