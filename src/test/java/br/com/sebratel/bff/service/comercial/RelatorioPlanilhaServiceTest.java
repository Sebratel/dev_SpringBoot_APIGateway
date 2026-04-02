package br.com.sebratel.bff.service.comercial;

import br.com.sebratel.bff.dto.comercial.PlanilhaInstalacaoDTO;
import br.com.sebratel.bff.repository.erp.comercial.RelatorioPlanilhaRepository;
import br.com.sebratel.bff.repository.erp.projections.comercial.PlanilhaInstalacaoProjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RelatorioPlanilhaServiceTest {

    @Mock
    private RelatorioPlanilhaRepository repository;

    @InjectMocks
    private RelatorioPlanilhaService service;

    @Test
    void deveListarPlanilhaInstalacaoFormatandoNomeParaMaiusculo() {
        String nomeEntrada = "  vendedor teste  ";
        String nomeFormatado = "VENDEDOR TESTE";
        PlanilhaInstalacaoProjection mockEntity = mock(PlanilhaInstalacaoProjection.class);

        when(repository.findPlanilhaInstalacao(nomeFormatado))
                .thenReturn(Stream.of(mockEntity));

        Stream<PlanilhaInstalacaoDTO> resultado = service.listarPlanilhaInstalacao(nomeEntrada);

        assertNotNull(resultado);
        assertEquals(1, resultado.count());
        verify(repository, times(1)).findPlanilhaInstalacao(nomeFormatado);
    }

    @Test
    void deveRetornarStreamVazioQuandoNaoHouverDados() {
        String nome = "VENDEDOR";
        when(repository.findPlanilhaInstalacao(nome)).thenReturn(Stream.empty());

        Stream<PlanilhaInstalacaoDTO> resultado = service.listarPlanilhaInstalacao(nome);

        assertEquals(0, resultado.count());
        verify(repository).findPlanilhaInstalacao(nome);
    }
}