package br.com.sebratel.bff.service;

import br.com.sebratel.bff.exceptions.ResourceNotFoundException;
import br.com.sebratel.bff.repository.erp.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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
        when(repository.findPersonIdsByEmail(email)).thenReturn(List.of(expectedId));

        // Act
        Long result = service.getPersonIdByEmail(email);

        // Assert
        assertEquals(expectedId, result);
        verify(repository, times(1)).findPersonIdsByEmail(email);
    }

    @Test
    void getPersonIdByEmail_ShouldReturnFirstOrdered_WhenEmailHasDuplicatePeople() {
        // Arrange — caso real que causava o HTTP 500: o mesmo e-mail em duas pessoas.
        // A query devolve o cadastro com CPF primeiro (89822) e o duplicado vazio depois (302328);
        // o service usa o primeiro sem estourar NonUniqueResultException.
        String email = "bruno.soares@sebratel.com.br";
        when(repository.findPersonIdsByEmail(email)).thenReturn(List.of(89822L, 302328L));

        // Act
        Long result = service.getPersonIdByEmail(email);

        // Assert
        assertEquals(89822L, result);
        verify(repository, times(1)).findPersonIdsByEmail(email);
    }

    @Test
    void getPersonIdByEmail_ShouldCollapseRepeatedSameId_WhenVUsersHasDuplicateRow() {
        // Arrange — v_users com a linha do e-mail duplicada faz o join repetir o MESMO id.
        // Não é ambiguidade real de pessoa: deduplicamos e devolvemos o único id.
        String email = "test@sebratel.com.br";
        when(repository.findPersonIdsByEmail(email)).thenReturn(List.of(89822L, 89822L));

        // Act
        Long result = service.getPersonIdByEmail(email);

        // Assert
        assertEquals(89822L, result);
        verify(repository, times(1)).findPersonIdsByEmail(email);
    }

    @Test
    void getPersonIdByEmail_ShouldThrowException_WhenEmailDoesNotExist() {
        // Arrange
        String email = "notfound@sebratel.com.br";
        when(repository.findPersonIdsByEmail(email)).thenReturn(List.of());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> service.getPersonIdByEmail(email));
        verify(repository, times(1)).findPersonIdsByEmail(email);
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
