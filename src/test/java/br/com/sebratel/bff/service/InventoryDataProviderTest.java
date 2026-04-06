package br.com.sebratel.bff.service;

import br.com.sebratel.bff.repository.erp.InventoryRepository;
import br.com.sebratel.bff.repository.erp.projections.InventoryProjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryDataProviderTest {

    @Mock
    private InventoryRepository repository;

    @InjectMocks
    private InventoryDataProvider provider;

    @Test
    void getFullInventory_ShouldReturnDataFromRepository() {
        // Arrange
        InventoryProjection projection = mock(InventoryProjection.class);
        when(repository.findAllEstoqueAgregado()).thenReturn(List.of(projection));

        // Act
        List<InventoryProjection> result = provider.getFullInventory();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repository, times(1)).findAllEstoqueAgregado();
    }

    @Test
    void getFullInventory_ShouldReturnEmptyList_WhenNoData() {
        // Arrange
        when(repository.findAllEstoqueAgregado()).thenReturn(List.of());

        // Act
        List<InventoryProjection> result = provider.getFullInventory();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository, times(1)).findAllEstoqueAgregado();
    }
}
