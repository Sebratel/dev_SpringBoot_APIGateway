package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.ContratoBloqueadoDTO;
import br.com.sebratel.bff.repository.erp.ContratoBloqueadoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContratoBloqueadoService {

    private final ContratoBloqueadoRepository repository;

    public ContratoBloqueadoService(ContratoBloqueadoRepository repository) {
        this.repository = repository;
    }

    public List<ContratoBloqueadoDTO> listarContratosBloqueados() {
        return repository.findContratosBloqueados().stream()
                .map(p -> new ContratoBloqueadoDTO(
                        p.getCliente(),
                        p.getContrato(),
                        p.getUsuario(),
                        p.getConcentrador(),
                        p.getPontoAcesso(),
                        p.getStatusContrato(),
                        p.getEstagioContrato(),
                        p.getSite(),
                        p.getStatusConexao(),
                        p.getSplitter(),
                        p.getCidade(),
                        p.getDiaBloqueio(),
                        p.getDiasBloqueados()
                )).toList();
    }
}