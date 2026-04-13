package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.DhoUserDTO;
import br.com.sebratel.bff.model.entity.DhoUserEntity;
import br.com.sebratel.bff.repository.afetados.DhoUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DhoUserService {

    private final DhoUserRepository repository;

    public List<DhoUserDTO> findAll() {
        log.info("Fetching all DHO users");
        return repository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private DhoUserDTO convertToDTO(DhoUserEntity entity) {
        return new DhoUserDTO(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getAccessLevel()
        );
    }
}
