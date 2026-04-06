package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.MatrixMassiveOutputDTO;
import br.com.sebratel.bff.model.entity.PersonEntity;
import br.com.sebratel.bff.model.entity.UsuarioAfetadoEntity;
import br.com.sebratel.bff.repository.afetados.UsuarioAfetadoRepository;
import br.com.sebratel.bff.repository.erp.PersonRepository;
import br.com.sebratel.bff.repository.erp.projections.ContractProjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatrixServiceTest {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private UsuarioAfetadoRepository usuarioAfetadoRepository;

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
        
        UsuarioAfetadoEntity affected = new UsuarioAfetadoEntity();
        affected.setFinishDate(LocalDateTime.now().plusHours(5));

        when(personRepository.findByTxId(cpf)).thenReturn(Optional.of(person));
        when(personRepository.findContractByCPF(cpf)).thenReturn(Optional.of(contract));
        when(usuarioAfetadoRepository.findByContractId(100L)).thenReturn(Optional.of(affected));

        // Act
        MatrixMassiveOutputDTO result = service.getContractInfoByCPF(cpf);

        // Assert
        assertEquals("client_found", result.getStatusCliente());
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
        assertEquals("not_found_client", result.getStatusCliente());
        assertEquals(0L, result.getAuthenticationProblems());
        assertEquals("23", result.getResolutionTimeHour());
    }

    @Test
    void getContractInfoByCPF_ShouldReturnNotFound_WhenContractDoesNotExist() {
        // Arrange
        String cpf = "12345678900";
        PersonEntity person = new PersonEntity();
        when(personRepository.findByTxId(cpf)).thenReturn(Optional.of(person));
        when(personRepository.findContractByCPF(cpf)).thenReturn(Optional.empty());

        // Act
        MatrixMassiveOutputDTO result = service.getContractInfoByCPF(cpf);

        // Assert
        assertEquals("not_found_client", result.getStatusCliente());
    }

    @Test
    void getContractInfoByCPF_ShouldReturnNotFound_WhenAffectedDoesNotExist() {
        // Arrange
        String cpf = "12345678900";
        PersonEntity person = new PersonEntity();
        ContractProjection contract = mock(ContractProjection.class);
        when(contract.getContractId()).thenReturn(100L);

        when(personRepository.findByTxId(cpf)).thenReturn(Optional.of(person));
        when(personRepository.findContractByCPF(cpf)).thenReturn(Optional.of(contract));
        when(usuarioAfetadoRepository.findByContractId(100L)).thenReturn(Optional.empty());

        // Act
        MatrixMassiveOutputDTO result = service.getContractInfoByCPF(cpf);

        // Assert
        assertEquals("not_found_client", result.getStatusCliente());
    }

    @Test
    void getContractInfoByCPF_ShouldHandleException() {
        // Arrange
        String cpf = "12345678900";
        when(personRepository.findByTxId(cpf)).thenThrow(new RuntimeException("Database error"));

        // Act
        MatrixMassiveOutputDTO result = service.getContractInfoByCPF(cpf);

        // Assert
        assertEquals("not_found_client", result.getStatusCliente());
    }
}
