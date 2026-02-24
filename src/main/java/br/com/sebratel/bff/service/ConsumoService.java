package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.ConsumoDTO;
import br.com.sebratel.bff.repository.erp.PlanoRepository;
import br.com.sebratel.bff.repository.erp.projections.PlanoProjection;
import br.com.sebratel.bff.repository.radius.ConsumoRepository;
import br.com.sebratel.bff.repository.radius.projections.ConsumoProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public Page<ConsumoDTO> listarConsumoAltoPaginado(int pagina, int tamanho) {
        Pageable pageable = PageRequest.of(pagina, tamanho, Sort.by("downloadTb").descending());

        // 1. Busca apenas a "fatia" (página) de usuários no Radius
        Page<ConsumoProjection> paginaRadius = radiusRepository.findConsumoExcedentePaginado(pageable);

        // 2. Extrai os usernames apenas desta página para buscar no ERP
        List<String> usernames = paginaRadius.getContent().stream()
                .map(ConsumoProjection::getUsername)
                .toList();

        // 3. Busca dados cadastrais no ERP apenas para esses usuários
        Map<String, PlanoProjection> mapaPlanos = erpRepository.findPlanosPorUsernames(usernames).stream()
                .collect(Collectors.toMap(
                        p -> p.getUsername().trim().toLowerCase(),
                        p -> p,
                        (existente, novo) -> existente
                ));

        // 4. Converte a página de Projections para uma página de DTOs
        return paginaRadius.map(c -> {
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
        });
    }

}