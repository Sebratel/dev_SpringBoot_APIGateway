package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.MatrixMassiveOutputDTO;
import br.com.sebratel.bff.dto.massivas.ImpactDetailsOutputDTO;
import br.com.sebratel.bff.dto.massivas.ImpactedUsersOutputDTO;
import br.com.sebratel.bff.exceptions.ResourceNotFoundException;
import br.com.sebratel.bff.model.entity.PersonEntity;
import br.com.sebratel.bff.repository.erp.PersonRepository;
import br.com.sebratel.bff.repository.erp.projections.ContractProjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatrixServiceTest {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private AffectedUserService affectedUserService;

    @InjectMocks
    private MatrixService service;

    private ImpactedUsersOutputDTO impact(Long contractId, long estimateTimeOfRestoration, long estimatedTimeHour) {
        return ImpactedUsersOutputDTO.builder()
                .impactedUsers(List.of(Map.of(contractId, ImpactDetailsOutputDTO.builder()
                        .reason("massiva")
                        .estimateTimeOfRestoration(estimateTimeOfRestoration)
                        .estimatedTimeHour(estimatedTimeHour)
                        .build())))
                .build();
    }

    // Implementacao direta em vez de mock: chamar when() dentro do argumento de outro
    // when() faz o Mockito acusar UnfinishedStubbing.
    private ContractProjection contract(Long contractId) {
        return () -> contractId;
    }

    @Test
    void getContractInfoByCPF_ShouldReturnClientFound_WhenAllExists() {
        String cpf = "12345678900";
        PersonEntity person = new PersonEntity();
        person.setId(1L);

        when(personRepository.findByTxId(cpf)).thenReturn(Optional.of(person));
        when(personRepository.findContractsByCPF(cpf)).thenReturn(List.of(contract(100L)));
        when(affectedUserService.getUsuariosAfetadosByContractId(100L)).thenReturn(impact(100L, 5L, 18L));

        MatrixMassiveOutputDTO result = service.getContractInfoByCPF(cpf);

        assertEquals("client_found", result.getStatus());
        assertEquals(1L, result.getAuthenticationProblems());
        assertEquals(5, result.getResolutionTime());
        assertEquals("18", result.getResolutionTimeHour());
    }

    @Test
    void getContractInfoByCPF_ShouldReturnNotFound_WhenPersonDoesNotExist() {
        String cpf = "00000000000";
        when(personRepository.findByTxId(cpf)).thenReturn(Optional.empty());

        MatrixMassiveOutputDTO result = service.getContractInfoByCPF(cpf);

        assertEquals("not_found_client", result.getStatus());
        assertEquals(0L, result.getAuthenticationProblems());
        assertEquals("23", result.getResolutionTimeHour());
    }

    @Test
    void getContractInfoByCPF_ShouldReturnNotFound_WhenContractDoesNotExist() {
        String cpf = "12345678900";
        when(personRepository.findByTxId(cpf)).thenReturn(Optional.of(new PersonEntity()));
        when(personRepository.findContractsByCPF(cpf)).thenReturn(List.of());

        MatrixMassiveOutputDTO result = service.getContractInfoByCPF(cpf);

        assertEquals("not_found_client", result.getStatus());
    }

    @Test
    void getContractInfoByCPF_ShouldReturnNotFound_WhenAffectedDoesNotExist() {
        String cpf = "12345678900";
        when(personRepository.findByTxId(cpf)).thenReturn(Optional.of(new PersonEntity()));
        when(personRepository.findContractsByCPF(cpf)).thenReturn(List.of(contract(100L)));
        when(affectedUserService.getUsuariosAfetadosByContractId(100L))
                .thenThrow(new ResourceNotFoundException("Usuário afetado não encontrado"));

        MatrixMassiveOutputDTO result = service.getContractInfoByCPF(cpf);

        assertEquals("not_found_client", result.getStatus());
    }

    @Test
    void getContractInfoByCPF_ShouldReturnClientFound_WhenAnyOfTheContractsIsAffected() {
        // Cenario que antes estourava IncorrectResultSizeDataAccessException, pois a query
        // devolvia mais de uma linha para um Optional. A excecao era engolida pelo catch e
        // o cliente afetado era reportado como not_found_client.
        String cpf = "12345678900";
        PersonEntity person = new PersonEntity();
        person.setId(1L);

        when(personRepository.findByTxId(cpf)).thenReturn(Optional.of(person));
        when(personRepository.findContractsByCPF(cpf)).thenReturn(List.of(contract(100L), contract(200L)));
        when(affectedUserService.getUsuariosAfetadosByContractId(100L))
                .thenThrow(new ResourceNotFoundException("Usuário afetado não encontrado"));
        when(affectedUserService.getUsuariosAfetadosByContractId(200L)).thenReturn(impact(200L, 3L, 21L));

        MatrixMassiveOutputDTO result = service.getContractInfoByCPF(cpf);

        assertEquals("client_found", result.getStatus());
        assertEquals(1L, result.getAuthenticationProblems());
        assertEquals(3, result.getResolutionTime());
        assertEquals("21", result.getResolutionTimeHour());
        verify(affectedUserService).getUsuariosAfetadosByContractId(100L);
        verify(affectedUserService).getUsuariosAfetadosByContractId(200L);
    }

    @Test
    void getContractInfoByCPF_ShouldStopAtFirstAffectedContract() {
        String cpf = "12345678900";
        when(personRepository.findByTxId(cpf)).thenReturn(Optional.of(new PersonEntity()));
        when(personRepository.findContractsByCPF(cpf)).thenReturn(List.of(contract(100L), contract(200L)));
        when(affectedUserService.getUsuariosAfetadosByContractId(100L)).thenReturn(impact(100L, 7L, 9L));

        MatrixMassiveOutputDTO result = service.getContractInfoByCPF(cpf);

        assertEquals("client_found", result.getStatus());
        assertEquals(7, result.getResolutionTime());
        verify(affectedUserService, never()).getUsuariosAfetadosByContractId(200L);
    }

    @Test
    void getContractInfoByCPF_ShouldReturnNotFound_WhenNoneOfTheContractsIsAffected() {
        String cpf = "12345678900";
        when(personRepository.findByTxId(cpf)).thenReturn(Optional.of(new PersonEntity()));
        when(personRepository.findContractsByCPF(cpf)).thenReturn(List.of(contract(100L), contract(200L)));
        when(affectedUserService.getUsuariosAfetadosByContractId(anyLong()))
                .thenThrow(new ResourceNotFoundException("Usuário afetado não encontrado"));

        MatrixMassiveOutputDTO result = service.getContractInfoByCPF(cpf);

        assertEquals("not_found_client", result.getStatus());
        assertEquals(0L, result.getAuthenticationProblems());
        assertEquals("23", result.getResolutionTimeHour());
    }

    @Test
    void getContractInfoByCPF_ShouldReturnNotFound_WhenImpactedUsersHasNoEntryForTheContract() {
        String cpf = "12345678900";
        when(personRepository.findByTxId(cpf)).thenReturn(Optional.of(new PersonEntity()));
        when(personRepository.findContractsByCPF(cpf)).thenReturn(List.of(contract(100L)));
        when(affectedUserService.getUsuariosAfetadosByContractId(100L)).thenReturn(impact(999L, 5L, 18L));

        MatrixMassiveOutputDTO result = service.getContractInfoByCPF(cpf);

        assertEquals("not_found_client", result.getStatus());
    }

    @Test
    void getContractInfoByCPF_ShouldHandleException() {
        String cpf = "12345678900";
        when(personRepository.findByTxId(cpf)).thenThrow(new RuntimeException("Database error"));

        MatrixMassiveOutputDTO result = service.getContractInfoByCPF(cpf);

        assertEquals("not_found_client", result.getStatus());
    }
}
