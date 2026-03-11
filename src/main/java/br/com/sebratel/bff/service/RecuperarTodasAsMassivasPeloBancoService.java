package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.massivas.MassivasBFFOutputDTO;
import br.com.sebratel.bff.repository.erp.RecuperarTodasAsMassivasPeloBancoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecuperarTodasAsMassivasPeloBancoService {

    private final RecuperarTodasAsMassivasPeloBancoRepository recuperarTodasAsMassivasPeloBancoRepository;

    @Autowired
    public RecuperarTodasAsMassivasPeloBancoService(RecuperarTodasAsMassivasPeloBancoRepository recuperarTodasAsMassivasPeloBancoRepository) {
        this.recuperarTodasAsMassivasPeloBancoRepository = recuperarTodasAsMassivasPeloBancoRepository;
    }

    public List<MassivasBFFOutputDTO> executar() {
        return this.recuperarTodasAsMassivasPeloBancoRepository.findActiveAssignments()
                .stream().map(p -> new MassivasBFFOutputDTO(
                        p.getID(),
                        p.getCRIACAO(),
                        p.getFINALIZADO(),
                        p.getPROTOCOLO(),
                        p.getEQUIPE(),
                        p.getSTATUS(),
                        p.getTIPO_SOLICITACAO(),
                        p.getSOLICITANTE(),
                        p.getRESPONSAVEL()
                )).toList();
    }
}
