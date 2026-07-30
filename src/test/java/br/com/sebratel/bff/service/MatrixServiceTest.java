package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.MatrixMassiveOutputDTO;
import br.com.sebratel.bff.model.entity.AffectedUsersEntity;
import br.com.sebratel.bff.model.entity.PersonEntity;
import br.com.sebratel.bff.repository.afetados.AffectedUserRepository;
import br.com.sebratel.bff.repository.erp.PersonRepository;
import br.com.sebratel.bff.repository.erp.projections.ContractProjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatrixServiceTest {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private AffectedUserRepository affectedUserRepository;

    @InjectMocks
    private MatrixService service;

    @Test
    void getContractInfoByCPF_ShouldReturnClientFound_WhenAllExists() {
        // Arrange
        String cpf = "12345678900";
        PersonEntity person = new PersonEntity();
        person.setId(1L);
        
        ContractProjection contract = mock(ContractProjection.class);
        when(contract.getContractId()).thenReturn(100L);
        
        AffectedUsersEntity affected = new AffectedUsersEntity();
        affected.setFinishDate(LocalDateTime.now().plusHours(5));

        when(personRepository.findByTxId(cpf)).thenReturn(Optional.of(person));
        when(personRepository.findContractsByCPF(cpf)).thenReturn(List.of(contract));
        when(affectedUserRepository.findFirstByContractId(100L)).thenReturn(Optional.of(affected));

        // Act
        MatrixMassiveOutputDTO result = service.getContractInfoByCPF(cpf);

        // Assert
        assertEquals("client_found", result.getStatus());
        assertEquals(1L, result.getAuthenticationProblems());
        assertNotNull(result.getResolutionTimeHour());
    }

    @Test
    void getContractInfoByCPF_ShouldReturnNotFound_WhenPersonDoesNotExist() {
        // Arrange
        String cpf = "00000000000";
        when(personRepository.findByTxId(cpf)).thenReturn(Optional.empty());

        // Act
        MatrixMassiveOutputDTO result = service.getContractInfoByCPF(cpf);

        // Assert
        assertEquals("not_found_client", result.getStatus());
        assertEquals(0L, result.getAuthenticationProblems());
        assertEquals("23", result.getResolutionTimeHour());
    }

    @Test
    void getContractInfoByCPF_ShouldReturnNotFound_WhenContractDoesNotExist() {
        // Arrange
        String cpf = "12345678900";
        PersonEntity person = new PersonEntity();
        when(personRepository.findByTxId(cpf)).thenReturn(Optional.of(person));
        when(personRepository.findContractsByCPF(cpf)).thenReturn(List.of());

        // Act
        MatrixMassiveOutputDTO result = service.getContractInfoByCPF(cpf);

        // Assert
        assertEquals("not_found_client", result.getStatus());
    }

    @Test
    void getContractInfoByCPF_ShouldReturnNotFound_WhenAffectedDoesNotExist() {
        // Arrange
        String cpf = "12345678900";
        PersonEntity person = new PersonEntity();
        ContractProjection contract = mock(ContractProjection.class);
        when(contract.getContractId()).thenReturn(100L);

        when(personRepository.findByTxId(cpf)).thenReturn(Optional.of(person));
        when(personRepository.findContractsByCPF(cpf)).thenReturn(List.of(contract));
        when(affectedUserRepository.findFirstByContractId(100L)).thenReturn(Optional.empty());

        // Act
        MatrixMassiveOutputDTO result = service.getContractInfoByCPF(cpf);

        // Assert
        assertEquals("not_found_client", result.getStatus());
    }

    @Test
    void getContractInfoByCPF_ShouldReturnClientFound_WhenCpfHasMultipleContractsAndOnlyTheLastIsAffected() {
        // Cenario que antes estourava IncorrectResultSizeDataAccessException, pois a query
        // devolvia mais de uma linha para um Optional. A excecao era engolida pelo catch e
        // o cliente afetado era reportado como not_found_client.
        String cpf = "12345678900";
        PersonEntity person = new PersonEntity();
        person.setId(1L);

        ContractProjection withoutMassive = mock(ContractProjection.class);
        when(withoutMassive.getContractId()).thenReturn(100L);
        ContractProjection withMassive = mock(ContractProjection.class);
        when(withMassive.getContractId()).thenReturn(200L);

        AffectedUsersEntity affected = new AffectedUsersEntity();
        affected.setFinishDate(LocalDateTime.now().plusHours(5));

        when(personRepository.findByTxId(cpf)).thenReturn(Optional.of(person));
        when(personRepository.findContractsByCPF(cpf)).thenReturn(List.of(withoutMassive, withMassive));
        when(affectedUserRepository.findFirstByContractId(100L)).thenReturn(Optional.empty());
        when(affectedUserRepository.findFirstByContractId(200L)).thenReturn(Optional.of(affected));

        MatrixMassiveOutputDTO result = service.getContractInfoByCPF(cpf);

        assertEquals("client_found", result.getStatus());
        assertEquals(1L, result.getAuthenticationProblems());
        verify(affectedUserRepository).findFirstByContractId(100L);
        verify(affectedUserRepository).findFirstByContractId(200L);
    }

    @Test
    void getContractInfoByCPF_ShouldReturnNotFound_WhenNoneOfTheContractsIsAffected() {
        String cpf = "12345678900";
        PersonEntity person = new PersonEntity();

        ContractProjection first = mock(ContractProjection.class);
        when(first.getContractId()).thenReturn(100L);
        ContractProjection second = mock(ContractProjection.class);
        when(second.getContractId()).thenReturn(200L);

        when(personRepository.findByTxId(cpf)).thenReturn(Optional.of(person));
        when(personRepository.findContractsByCPF(cpf)).thenReturn(List.of(first, second));
        when(affectedUserRepository.findFirstByContractId(anyLong())).thenReturn(Optional.empty());

        MatrixMassiveOutputDTO result = service.getContractInfoByCPF(cpf);

        assertEquals("not_found_client", result.getStatus());
        assertEquals(0L, result.getAuthenticationProblems());
        assertEquals("23", result.getResolutionTimeHour());
    }

    @Test
    void getContractInfoByCPF_ShouldHandleException() {
        // Arrange
        String cpf = "12345678900";
        when(personRepository.findByTxId(cpf)).thenThrow(new RuntimeException("Database error"));

        // Act
        MatrixMassiveOutputDTO result = service.getContractInfoByCPF(cpf);

        // Assert
        assertEquals("not_found_client", result.getStatus());
    }
    @Test
    void getContractInfoByCPF_ShouldReturnResolutionTimeOne_WhenFinishDateIsInPast() {
        // Arrange
        String cpf = "12345678900";
        PersonEntity person = new PersonEntity();
        person.setId(1L);

        ContractProjection contract = mock(ContractProjection.class);
        when(contract.getContractId()).thenReturn(100L);

        AffectedUsersEntity affected = new AffectedUsersEntity();
        affected.setFinishDate(LocalDateTime.now().minusHours(2)); // Branch: hoursBetween <= 0

        when(personRepository.findByTxId(cpf)).thenReturn(Optional.of(person));
        when(personRepository.findContractsByCPF(cpf)).thenReturn(List.of(contract));
        when(affectedUserRepository.findFirstByContractId(100L)).thenReturn(Optional.of(affected));

        // Act
        MatrixMassiveOutputDTO result = service.getContractInfoByCPF(cpf);

        // Assert
        assertEquals("client_found", result.getStatus());
        assertEquals(1, result.getResolutionTime()); // Branch: hoursBetween <= 0 ? 1 : (int) hoursBetween
    }
}

