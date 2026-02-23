package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.ConsumoDTO;
import br.com.sebratel.bff.repository.erp.PlanoRepository;
import br.com.sebratel.bff.repository.erp.projections.PlanoProjection;
import br.com.sebratel.bff.repository.radius.ConsumoRepository;
import br.com.sebratel.bff.repository.radius.projections.ConsumoProjection;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ConsumoService {

    private final ConsumoRepository radiusRepository;
    private final PlanoRepository erpRepository;

    public ConsumoService(ConsumoRepository radiusRepository, PlanoRepository erpRepository) {
        this.radiusRepository = radiusRepository;
        this.erpRepository = erpRepository;
    }

    public List<ConsumoDTO> listarConsumoAlto() {
        List<ConsumoProjection> consumos = radiusRepository.findConsumoExcedente();

        Map<String, PlanoProjection> mapaPlanos = erpRepository.findTodosPlanos().stream()
                .filter(p -> p.getUsername() != null)
                .collect(Collectors.toMap(
                        p -> p.getUsername().trim().toLowerCase(),
                        p -> p,
                        (existente, novo) -> existente
                ));

        return consumos.stream().map(c -> {
            String userKey = c.getUsername().trim().toLowerCase();
            PlanoProjection plano = mapaPlanos.get(userKey);

            return new ConsumoDTO(
                    c.getUsername(),
                    plano != null ? plano.getCliente() : "N/D",
                    plano != null ? plano.getContrato() : "N/D",
                    plano != null ? plano.getPlano() : "N/D",
                    c.getDownloadTb(),
                    c.getUploadTb(),
                    c.getTotalTb()
            );
        }).toList();
    }
}