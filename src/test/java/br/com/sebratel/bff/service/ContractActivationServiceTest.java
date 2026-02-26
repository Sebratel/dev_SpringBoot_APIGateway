package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.ContractActivationDTO;
import br.com.sebratel.bff.repository.erp.ContractActivationRepository;
import br.com.sebratel.bff.repository.erp.projections.ContractActivationProjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractActivationServiceTest {

    @Mock
    private ContractActivationRepository repository;

    @InjectMocks
    private ContractActivationService service;

    @Test
    void deveRetornarRelatorioDeAtivacaoComSucesso() {
        ContractActivationProjection p = mock(ContractActivationProjection.class);
        LocalDateTime dataCriacao = LocalDateTime.of(2023, 1, 1, 10, 0);
        LocalDateTime dataAtivacao = LocalDateTime.of(2023, 1, 5, 14, 30);

        when(p.getDataCriacaoContrato()).thenReturn(dataCriacao);
        when(p.getClientes()).thenReturn("Cliente Teste");
        when(p.getStatusContrato()).thenReturn("Ativo");
        when(p.getStatusCancelamento()).thenReturn(null);
        when(p.getContrato()).thenReturn("12345");
        when(p.getVendedor()).thenReturn("Vendedor Alpha");
        when(p.getTecnologia()).thenReturn("FTTH");
        when(p.getDataAtivacao()).thenReturn(dataAtivacao);

        when(repository.findMergedContractData()).thenReturn(List.of(p));

        List<ContractActivationDTO> resultado = service.getActivationReport();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());

        ContractActivationDTO dto = resultado.get(0);
        assertEquals(dataCriacao.toString(), dto.getDataCriacaoContrato());
        assertEquals("Cliente Teste", dto.getClientes());
        assertEquals("Ativo", dto.getStatusContrato());
        assertNull(dto.getStatusCancelamento());
        assertEquals("12345", dto.getContrato());
        assertEquals("Vendedor Alpha", dto.getVendedor());
        assertEquals("FTTH", dto.getTecnologia());
        assertEquals(dataAtivacao.toString(), dto.getDataAtivacao());

        verify(repository, times(1)).findMergedContractData();
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverDados() {
        when(repository.findMergedContractData()).thenReturn(List.of());

        List<ContractActivationDTO> resultado = service.getActivationReport();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(repository, times(1)).findMergedContractData();
    }

    @Test
    void deveLancarExcecaoQuandoDataForNula() {
        ContractActivationProjection p = mock(ContractActivationProjection.class);
        when(p.getDataCriacaoContrato()).thenReturn(null);
        when(repository.findMergedContractData()).thenReturn(List.of(p));

        assertThrows(NullPointerException.class, () -> {
            service.getActivationReport();
        });
    }
}