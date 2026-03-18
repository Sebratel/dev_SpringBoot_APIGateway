package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.massivas.ImpactDetailsOutputDTO;
import br.com.sebratel.bff.dto.massivas.ImpactedUsersOutputDTO;
import br.com.sebratel.bff.exceptions.ResourceNotFoundException;
import br.com.sebratel.bff.model.entity.UsuarioAfetadoEntity;
import br.com.sebratel.bff.repository.afetados.UsuarioAfetadoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class UsuarioAfetadoService {

    private final UsuarioAfetadoRepository usuarioAfetadoRepository;

    @Autowired
    public UsuarioAfetadoService(UsuarioAfetadoRepository usuarioAfetadoRepository) {
        this.usuarioAfetadoRepository = usuarioAfetadoRepository;
    }

    @Transactional(transactionManager = "afetadosTransactionManager")
    public ImpactedUsersOutputDTO createImpactedUsersDTO(List<UsuarioAfetadoEntity> input) {
        log.info("Salvando lista de {} usuários afetados", input.size());
        List<UsuarioAfetadoEntity> usuarioAfetadoEntities = usuarioAfetadoRepository.saveAll(input);
        log.info("Usuários afetados para o protocolo criados com sucesso.");
        return getImpactedUsersDTO(usuarioAfetadoEntities);
    }

    private ImpactedUsersOutputDTO getImpactedUsersDTO(List<UsuarioAfetadoEntity> usuariosAfetados) {
        log.debug("Criando DTO de usuários afetados.{}", usuariosAfetados);
        List<Map<Long, ImpactDetailsOutputDTO>> impactedUsersDTO = usuariosAfetados.stream().map(usuarioAfetado -> {
            LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
            LocalDateTime finish = usuarioAfetado.getFinishDate();
            long hoursRemaining = ChronoUnit.HOURS.between(now, finish);
            long estimateTimeOfRestoration = Math.max(0, hoursRemaining);

            ImpactDetailsOutputDTO impactDetailsDTO = ImpactDetailsOutputDTO.builder()
                    .reason(usuarioAfetado.getReason())
                    .estimateTimeOfRestoration(estimateTimeOfRestoration)
                    .build();
            Map<Long, ImpactDetailsOutputDTO> impactedUsers = new HashMap<>();
            impactedUsers.put(usuarioAfetado.getContractId(), impactDetailsDTO);
            return impactedUsers;
        }).toList();

        return ImpactedUsersOutputDTO.builder()
                .impactedUsers(impactedUsersDTO)
                .build();
    }

    public ImpactedUsersOutputDTO getAll() {
        log.info("Buscando usuários afetados");
        return getImpactedUsersDTO(usuarioAfetadoRepository.findAll());
    }

    public ImpactedUsersOutputDTO getUsuariosByProtocol(Long protocol) {
        log.info("Buscando usuários afetados pelo protocolo: {}", protocol);
        return getImpactedUsersDTO(usuarioAfetadoRepository.findByProtocol(protocol));
    }

    public ImpactedUsersOutputDTO getUsuariosAfetadosByPppoe(String pppoe) {
        log.info("Buscando usuário afetado pelo PPPoE: {}", pppoe);
        UsuarioAfetadoEntity usuarioAfetadoEntity = usuarioAfetadoRepository.findByPppoe(pppoe).orElseThrow(() -> new ResourceNotFoundException("Usuário afetado não encontrado"));
        log.info("Usuário afetado encontrado: {}", usuarioAfetadoEntity);
        return getImpactedUsersDTO(List.of(usuarioAfetadoEntity));
    }

    public void removeUsersByProtocol(Long protocol) {
        log.info("Removendo usuários do protocolo: {}", protocol);
        Integer numeroDeLinhasAfetadas = usuarioAfetadoRepository.deleteByProtocol(protocol);
        log.info("{} linhas foram afetadas na deleção dos usuarios de protocolo {}", numeroDeLinhasAfetadas, protocol);
    }

    @Transactional(transactionManager = "afetadosTransactionManager")
    public void alterarDataEstimadaParaFinalizacao(Long protocol, LocalDateTime finishDate) {
        log.info("Alterando data estimada para finalização");
        List<UsuarioAfetadoEntity> affectedUsers = usuarioAfetadoRepository.findByProtocol(protocol);
        if (affectedUsers.isEmpty()) {
            log.warn("Nenhum usuário afetado encontrado para o protocolo: {}", protocol);
            throw new ResourceNotFoundException("Nenhum usuário afetado encontrado para o protocolo: " + protocol);
        }
        log.info("Foram encontrados {} usuarios no protocolo {}", affectedUsers.size(), protocol);
        Integer totalDeLinhasAfetadas = usuarioAfetadoRepository.updateUsersByProtocol(protocol, finishDate);
        log.info("Data de finalização alterada para {} nos usuarios de protocolo {}", finishDate, protocol);

        if (totalDeLinhasAfetadas == 0) {
            log.warn("Nenhuma linha afetada para o protocolo {} ", protocol);
        }
    }

    public ImpactedUsersOutputDTO getUsuariosAfetadosByContractId(Long contractId) {
        log.info("Buscando usuário afetado pelo email: {}", contractId);
        UsuarioAfetadoEntity usuarioAfetadoEntity = usuarioAfetadoRepository.findByContractId(contractId).orElseThrow(() -> new ResourceNotFoundException("Usuário afetado não encontrado"));
        log.info("Usuário afetado encontrado por email: {}", usuarioAfetadoEntity);
        return getImpactedUsersDTO(List.of(usuarioAfetadoEntity));
    }
}
