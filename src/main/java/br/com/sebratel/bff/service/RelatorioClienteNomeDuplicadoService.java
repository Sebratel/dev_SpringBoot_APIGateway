package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.RelatorioClienteNomeDuplicadoDTO;
import br.com.sebratel.bff.repository.erp.RelatorioClienteNomeDuplicadoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RelatorioClienteNomeDuplicadoService {

    private final RelatorioClienteNomeDuplicadoRepository repository;

    public RelatorioClienteNomeDuplicadoService(RelatorioClienteNomeDuplicadoRepository repository) {
        this.repository = repository;
    }

    public List<RelatorioClienteNomeDuplicadoDTO> listarClientesNomesDuplicados() {
        return repository.findClientesNomesDuplicados().stream()
                .map(p -> new RelatorioClienteNomeDuplicadoDTO(
                        p.getAuthenticatedUser(),
                        p.getAuthContractDescription(),
                        p.getEventDescription()
                )).toList();
    }
}