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
        UsuarioAfetadoEntity user = new UsuarioAfetadoEntity();
        user.setContractId(1L);
        user.setFinishDate(LocalDateTime.now().plusHours(3));
        user.setReason("Maintenance");
        
        when(repository.findAll()).thenReturn(List.of(user));

        // Act
        ImpactedUsersOutputDTO result = service.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getImpactedUsers().size());
        assertTrue(result.getImpactedUsers().get(0).containsKey(1L));
        assertEquals("Maintenance", result.getImpactedUsers().get(0).get(1L).getReason());
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
