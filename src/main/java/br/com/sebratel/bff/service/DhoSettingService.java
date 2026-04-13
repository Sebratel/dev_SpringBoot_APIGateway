package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.DhoSettingDTO;
import br.com.sebratel.bff.model.entity.DhoSettingEntity;
import br.com.sebratel.bff.repository.afetados.DhoSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DhoSettingService {

    private final DhoSettingRepository repository;

    public List<DhoSettingDTO> findAll() {
        log.info("Fetching all DHO settings");
        return repository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private DhoSettingDTO convertToDTO(DhoSettingEntity entity) {
        return new DhoSettingDTO(
                entity.getCargo(),
                entity.getTime(),
                entity.getMotivo(),
                entity.getArea(),
                entity.getLocal(),
                entity.getStatusVaga(),
                entity.getRecrutador(),
                entity.getTipoDeDemissao(),
                entity.getMotivacao(),
                entity.getSituacao(),
                entity.getEscolaridade(),
                entity.getEtapa(),
                entity.getFonte(),
                entity.getGestor()
        );
    }
}
