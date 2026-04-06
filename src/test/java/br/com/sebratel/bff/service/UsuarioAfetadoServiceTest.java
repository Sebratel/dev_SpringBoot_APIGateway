package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.massivas.ImpactedUsersOutputDTO;
import br.com.sebratel.bff.exceptions.ResourceNotFoundException;
import br.com.sebratel.bff.model.entity.UsuarioAfetadoEntity;
import br.com.sebratel.bff.repository.afetados.UsuarioAfetadoRepository;
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
class UsuarioAfetadoServiceTest {

    @Mock
    private UsuarioAfetadoRepository repository;

    @InjectMocks
    private UsuarioAfetadoService service;

    @Test
    void getAll_ShouldReturnMappedDTO() {
        // Arrange
        UsuarioAfetadoEntity user1 = new UsuarioAfetadoEntity();
        user1.setContractId(1L);
        user1.setFinishDate(LocalDateTime.now().plusHours(3));
        user1.setReason("Maintenance");

        UsuarioAfetadoEntity user2 = new UsuarioAfetadoEntity();
        user2.setContractId(2L);
        user2.setFinishDate(LocalDateTime.now().minusHours(1)); // hoursRemaining <= 0
        user2.setReason("Outage");
        
        when(repository.findAll()).thenReturn(List.of(user1, user2));

        // Act
        ImpactedUsersOutputDTO result = service.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getImpactedUsers().size());
        assertEquals(3L, result.getImpactedUsers().get(0).get(1L).getEstimateTimeOfRestoration());
        assertEquals(2L, result.getImpactedUsers().get(1).get(2L).getEstimateTimeOfRestoration()); // Should be 2 because estimateTimeOfRestoration <= 0
    }

    @Test
    void createImpactedUsersDTO_ShouldSaveAndReturnDTO() {
        // Arrange
        UsuarioAfetadoEntity user = new UsuarioAfetadoEntity();
        user.setContractId(1L);
        user.setFinishDate(LocalDateTime.now().plusHours(5));
        List<UsuarioAfetadoEntity> input = List.of(user);
        when(repository.saveAll(input)).thenReturn(input);

        // Act
        ImpactedUsersOutputDTO result = service.createImpactedUsersDTO(input);

        // Assert
        assertNotNull(result);
        verify(repository).saveAll(input);
    }

    @Test
    void getUsuariosAfetadosByContractId_ShouldReturnUser_WhenExists() {
        // Arrange
        Long contractId = 1L;
        UsuarioAfetadoEntity user = new UsuarioAfetadoEntity();
        user.setContractId(contractId);
        user.setFinishDate(LocalDateTime.now().plusHours(3));
        when(repository.findByContractId(contractId)).thenReturn(Optional.of(user));

        // Act
        ImpactedUsersOutputDTO result = service.getUsuariosAfetadosByContractId(contractId);

        // Assert
        assertNotNull(result);
    }

    @Test
    void getUsuariosAfetadosByContractId_ShouldThrowException_WhenNotFound() {
        // Arrange
        Long contractId = 999L;
        when(repository.findByContractId(contractId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> service.getUsuariosAfetadosByContractId(contractId));
    }

    @Test
    void alterarDataEstimadaParaFinalizacao_ShouldLogWarn_WhenNoRowsAffected() {
        // Arrange
        Long protocol = 123L;
        LocalDateTime finish = LocalDateTime.now().plusDays(1);
        when(repository.findByProtocol(protocol)).thenReturn(List.of(new UsuarioAfetadoEntity()));
        when(repository.updateUsersByProtocol(protocol, finish)).thenReturn(0);

        // Act
        service.alterarDataEstimadaParaFinalizacao(protocol, finish);

        // Assert
        verify(repository).updateUsersByProtocol(protocol, finish);
    }

    @Test
    void getUsuariosByProtocol_ShouldReturnFilteredData() {
        // Arrange
        Long protocol = 123L;
        UsuarioAfetadoEntity user = new UsuarioAfetadoEntity();
        user.setContractId(1L);
        user.setFinishDate(LocalDateTime.now().plusHours(3));
        
        when(repository.findByProtocol(protocol)).thenReturn(List.of(user));

        // Act
        ImpactedUsersOutputDTO result = service.getUsuariosByProtocol(protocol);

        // Assert
        assertEquals(1, result.getImpactedUsers().size());
    }

    @Test
    void getUsuariosAfetadosByPppoe_ShouldReturnUser_WhenExists() {
        // Arrange
        String pppoe = "user@test";
        UsuarioAfetadoEntity user = new UsuarioAfetadoEntity();
        user.setContractId(1L);
        user.setFinishDate(LocalDateTime.now().plusHours(3));
        
        when(repository.findByPppoe(pppoe)).thenReturn(Optional.of(user));

        // Act
        ImpactedUsersOutputDTO result = service.getUsuariosAfetadosByPppoe(pppoe);

        // Assert
        assertNotNull(result);
    }

    @Test
    void getUsuariosAfetadosByPppoe_ShouldThrowException_WhenNotFound() {
        // Arrange
        String pppoe = "notfound@test";
        when(repository.findByPppoe(pppoe)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> service.getUsuariosAfetadosByPppoe(pppoe));
    }

    @Test
    void removeUsersByProtocol_ShouldCallRepository() {
        // Arrange
        Long protocol = 123L;
        when(repository.deleteByProtocol(protocol)).thenReturn(5);

        // Act
        service.removeUsersByProtocol(protocol);

        // Assert
        verify(repository).deleteByProtocol(protocol);
    }

    @Test
    void alterarDataEstimadaParaFinalizacao_ShouldUpdate_WhenUsersExist() {
        // Arrange
        Long protocol = 123L;
        LocalDateTime finish = LocalDateTime.now().plusDays(1);
        when(repository.findByProtocol(protocol)).thenReturn(List.of(new UsuarioAfetadoEntity()));
        when(repository.updateUsersByProtocol(protocol, finish)).thenReturn(1);

        // Act
        service.alterarDataEstimadaParaFinalizacao(protocol, finish);

        // Assert
        verify(repository).updateUsersByProtocol(protocol, finish);
    }

    @Test
    void alterarDataEstimadaParaFinalizacao_ShouldThrowException_WhenNoUsers() {
        // Arrange
        Long protocol = 123L;
        when(repository.findByProtocol(protocol)).thenReturn(List.of());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> service.alterarDataEstimadaParaFinalizacao(protocol, LocalDateTime.now()));
    }
}
