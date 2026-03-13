package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.massivas.ImpactDetailsDTO;
import br.com.sebratel.bff.dto.massivas.ImpactedUsersDTO;
import br.com.sebratel.bff.exceptions.ResourceNotFoundException;
import br.com.sebratel.bff.model.entity.UsuarioAfetado;
import br.com.sebratel.bff.repository.afetados.UsuarioAfetadoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public ImpactedUsersDTO createImpactedUsersDTO(List<UsuarioAfetado> input) {
        log.info("Salvando lista de {} usuários afetados", input.size());
        List<UsuarioAfetado> usuarioAfetados = usuarioAfetadoRepository.saveAll(input);
        log.info("Usuários afetados para o protocolo criados com sucesso.");
        return getImpactedUsersDTO(usuarioAfetados);
    }

    private ImpactedUsersDTO getImpactedUsersDTO(List<UsuarioAfetado> usuariosAfetados) {
        log.debug("Criando DTO de usuários afetados.{}", usuariosAfetados);
        List<Map<String, ImpactDetailsDTO>> impactedUsersDTO = usuariosAfetados.stream().map(usuarioAfetado -> {
            ImpactDetailsDTO impactDetailsDTO = ImpactDetailsDTO.builder()
                    .reason(usuarioAfetado.getReason())
                    .estimateTimeOfRestoration(usuarioAfetado.getFinishDate())
                    .build();
            Map<String, ImpactDetailsDTO> impactedUsers = new HashMap<>();
            impactedUsers.put(usuarioAfetado.getPppoe(), impactDetailsDTO);
            return impactedUsers;
        }).toList();

        return ImpactedUsersDTO.builder()
                .impactedUsers(impactedUsersDTO)
                .build();
    }

    public ImpactedUsersDTO getAll() {
        log.info("Buscando usuários afetados");
        return getImpactedUsersDTO(usuarioAfetadoRepository.findAll());
    }

    public ImpactedUsersDTO getUsuariosByProtocol(Long protocol) {
        log.info("Buscando usuários afetados pelo protocolo: {}", protocol);
        return getImpactedUsersDTO(usuarioAfetadoRepository.findByProtocol(protocol));
    }

    public ImpactedUsersDTO getUsuariosAfetadosByPppoe(String pppoe) {
        log.info("Buscando usuário afetado pelo PPPoE: {}", pppoe);
        UsuarioAfetado usuarioAfetado = usuarioAfetadoRepository.findByPppoe(pppoe).orElseThrow(() -> new ResourceNotFoundException("Usuário afetado não encontrado"));
        log.info("Usuário afetado encontrado: {}", usuarioAfetado);
        return getImpactedUsersDTO(List.of(usuarioAfetado));
    }

    public void removeUsersByProtocol(Long protocol) {
        log.info("Removendo usuários do protocolo: {}", protocol);
        Integer numeroDeLinhasAfetadas = usuarioAfetadoRepository.deleteByProtocol(protocol);
        log.info("{} linhas foram afetadas na deleção dos usuarios de protocolo {}", numeroDeLinhasAfetadas, protocol);
    }
}

