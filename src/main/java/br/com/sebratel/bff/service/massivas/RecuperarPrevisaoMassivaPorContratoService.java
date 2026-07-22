package br.com.sebratel.bff.service.massivas;

import br.com.sebratel.bff.dto.massivas.PrevisaoMassivaOutputDTO;
import br.com.sebratel.bff.exceptions.ResourceNotFoundException;
import br.com.sebratel.bff.repository.erp.massivas.RecuperarPrevisaoMassivaPorContratoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RecuperarPrevisaoMassivaPorContratoService {

    private final RecuperarPrevisaoMassivaPorContratoRepository recuperarPrevisaoMassivaPorContratoRepository;

    @Autowired
    public RecuperarPrevisaoMassivaPorContratoService(RecuperarPrevisaoMassivaPorContratoRepository recuperarPrevisaoMassivaPorContratoRepository) {
        this.recuperarPrevisaoMassivaPorContratoRepository = recuperarPrevisaoMassivaPorContratoRepository;
    }

    public PrevisaoMassivaOutputDTO executar(String contractNumber) {
        log.info("Buscando previsão de finalização da massiva no Voalle pelo número do contrato: {}", contractNumber);
        return recuperarPrevisaoMassivaPorContratoRepository.findEstimatedEndByContractNumber(contractNumber)
                .map(p -> new PrevisaoMassivaOutputDTO(
                        p.getID(),
                        p.getCONTRATO(),
                        p.getPROTOCOLO(),
                        p.getCRIACAO(),
                        p.getPREVISAO(),
                        p.getFINALIZADO(),
                        p.getSTATUS(),
                        p.getTIPO_SOLICITACAO(),
                        p.getDESCRICAO()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nenhum evento massivo ativo encontrado no Voalle para o contrato: " + contractNumber));
    }
}
