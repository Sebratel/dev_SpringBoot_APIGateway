package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.ContractFirstPaymentDTO;
import br.com.sebratel.bff.repository.erp.ContractPaymentRepository;
import br.com.sebratel.bff.repository.erp.projections.ContractFirstPaymentProjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractPaymentServiceTest {

    @Mock
    private ContractPaymentRepository repository;

    @InjectMocks
    private ContractPaymentService service;

    @Test
    void deveRetornarRelatorioDePagamentoComMapeamentoCorreto() {
        ContractFirstPaymentProjection p = mock(ContractFirstPaymentProjection.class);
        LocalDate agora = LocalDate.now();

        when(p.getNome()).thenReturn("Cliente Teste");
        when(p.getNumero_Contrato()).thenReturn("123");
        when(p.getPrimeira_Emissao()).thenReturn(agora);
        when(p.getPagamento_Cliente()).thenReturn(agora.plusDays(1));
        when(p.getData_Criacao()).thenReturn(agora.atStartOfDay().minusDays(5));
        when(p.getContractnumber()).thenReturn("456");
        when(p.getDescription()).thenReturn("Descricao teste");
        when(p.getStatus()).thenReturn("PAGO");

        when(repository.findFirstContractPayments()).thenReturn(List.of(p));

        List<ContractFirstPaymentDTO> resultado = service.getFirstPaymentReport();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());

        ContractFirstPaymentDTO dto = resultado.get(0);
        assertEquals("Cliente Teste", dto.getNome());
        assertEquals("123", dto.getNumeroContrato());
        assertEquals(agora, dto.getPrimeiraEmissao());
        assertEquals(agora.plusDays(1), dto.getPagamentoCliente());
        assertEquals(agora.minusDays(5), dto.getDataCriacao().toLocalDate());
        assertEquals("456", dto.getContractNumber());
        assertEquals("Descricao teste", dto.getDescription());
        assertEquals("PAGO", dto.getStatus());

        verify(repository, times(1)).findFirstContractPayments();
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverPagamentos() {
        when(repository.findFirstContractPayments()).thenReturn(List.of());

        List<ContractFirstPaymentDTO> resultado = service.getFirstPaymentReport();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(repository, times(1)).findFirstContractPayments();
    }

    @Test
    void deveMapearCamposNulosSemExplodir() {
        ContractFirstPaymentProjection p = mock(ContractFirstPaymentProjection.class);
        when(p.getNome()).thenReturn(null);
        when(p.getNumero_Contrato()).thenReturn(null);

        when(repository.findFirstContractPayments()).thenReturn(List.of(p));

        List<ContractFirstPaymentDTO> resultado = service.getFirstPaymentReport();

        assertNull(resultado.get(0).getNome());
        assertNull(resultado.get(0).getNumeroContrato());
    }
}