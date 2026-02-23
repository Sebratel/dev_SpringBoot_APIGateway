package br.com.sebratel.bff.service;


import br.com.sebratel.bff.dto.ContratoAtivacaoFaturaDTO;
import br.com.sebratel.bff.repository.erp.ContratoAtivacaoFaturaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContratoAtivacaoFaturaService {

    private final ContratoAtivacaoFaturaRepository repository;

    public ContratoAtivacaoFaturaService(ContratoAtivacaoFaturaRepository repository) {
        this.repository = repository;
    }

    public List<ContratoAtivacaoFaturaDTO> listarContratosRelacionados() {
        return repository.findContratosAtivacaoFatura().stream()
                .map(p -> new ContratoAtivacaoFaturaDTO(
                        p.getContrato(),
                        p.getVendedor(),
                        p.getNome(),
                        p.getDataAtivacao(),
                        p.getVencimento()
                )).toList();
    }
}