package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.AquisicaoDTO;
import br.com.sebratel.bff.repository.erp.AquisicaoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AquisicaoService {

    private final AquisicaoRepository repository;

    public AquisicaoService(AquisicaoRepository repository) {
        this.repository = repository;
    }

    public List<AquisicaoDTO> listarAquisicoes() {
        return repository.findAquisicoesPendentes().stream()
                .map(p -> new AquisicaoDTO(
                        p.getId(),
                        p.getCodigo(),
                        p.getProduto(),
                        p.getData(),
                        p.getRequisitadoPor(),
                        p.getDataPrevisao(),
                        p.getUnidades(),
                        p.getBase(),
                        p.getStatus(),
                        p.getOutrosStatus()
                )).collect(Collectors.toList());
    }
}