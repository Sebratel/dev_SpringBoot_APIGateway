package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.RelatorioFinalDTO;
import br.com.sebratel.bff.repository.erp.ContractActivationRepository;
import br.com.sebratel.bff.repository.erp.projections.ContractActivationProjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractDataServiceTest {

    @Mock
    private ContractActivationRepository repository;

    @InjectMocks
    private ContractDataService service;

    @Test
    void getDadosCompletosCache_ShouldReturnMappedDTOs() {
        // Arrange
        ContractActivationProjection p = mock(ContractActivationProjection.class);
        LocalDateTime date = LocalDateTime.of(2023, 10, 5, 10, 0); // October -> Outubro
        
        when(p.getClientes()).thenReturn("Client A");
        when(p.getContrato()).thenReturn("123");
        when(p.getDataCriacaoContrato()).thenReturn(date);
        when(p.getDataAtivacao()).thenReturn(date.plusDays(1));
        when(p.getVendedor()).thenReturn("Seller X");
        when(p.getTecnologia()).thenReturn("Fiber");
        when(p.getStatusContrato()).thenReturn("Active");
        when(p.getStatusCancelamento()).thenReturn("N/A");

        when(repository.findMergedContractData()).thenReturn(List.of(p));

        // Act
        List<RelatorioFinalDTO> result = service.getDadosCompletosCache();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        RelatorioFinalDTO dto = result.get(0);
        assertEquals("Client A", dto.clientes());
        assertEquals("Outubro", dto.mesDaCriacao());
        verify(repository, times(1)).findMergedContractData();
    }

    @Test
    void getDadosCompletosCache_ShouldHandleDuplicateKeys() {
        // Arrange
        ContractActivationProjection p1 = mock(ContractActivationProjection.class);
        when(p1.getClientes()).thenReturn("Client A");
        when(p1.getContrato()).thenReturn("123");
        
        ContractActivationProjection p2 = mock(ContractActivationProjection.class);
        when(p2.getClientes()).thenReturn("Client A");
        when(p2.getContrato()).thenReturn("123");

        when(repository.findMergedContractData()).thenReturn(List.of(p1, p2));

        // Act
        List<RelatorioFinalDTO> result = service.getDadosCompletosCache();

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    void getDadosCompletosCache_ShouldReturnEmptyList_WhenNoData() {
        // Arrange
        when(repository.findMergedContractData()).thenReturn(List.of());

        // Act
        List<RelatorioFinalDTO> result = service.getDadosCompletosCache();

        // Assert
        assertTrue(result.isEmpty());
    }
}
