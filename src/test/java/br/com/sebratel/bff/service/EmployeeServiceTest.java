package br.com.sebratel.bff.service;

import br.com.sebratel.bff.exceptions.ResourceNotFoundException;
import br.com.sebratel.bff.repository.erp.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository repository;

    @InjectMocks
    private EmployeeService service;

    @Test
    void getPersonIdByEmail_ShouldReturnId_WhenEmailExists() {
        // Arrange
        String email = "test@sebratel.com.br";
        Long expectedId = 123L;
        when(repository.findPersonIdByEmail(email)).thenReturn(Optional.of(expectedId));

        // Act
        Long result = service.getPersonIdByEmail(email);

        // Assert
        assertEquals(expectedId, result);
        verify(repository, times(1)).findPersonIdByEmail(email);
    }

    @Test
    void getPersonIdByEmail_ShouldThrowException_WhenEmailDoesNotExist() {
        // Arrange
        String email = "notfound@sebratel.com.br";
        when(repository.findPersonIdByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> service.getPersonIdByEmail(email));
        verify(repository, times(1)).findPersonIdByEmail(email);
    }

    @Test
    void hasB2BinInput_ShouldReturnRepositoryResponse() {
        // Arrange
        List<Long> ids = List.of(1L, 2L);
        when(repository.hasB2BinInput(ids)).thenReturn(true);

        // Act
        boolean result = service.hasB2BinInput(ids);

        // Assert
        assertTrue(result);
        verify(repository, times(1)).hasB2BinInput(ids);
    }
}
