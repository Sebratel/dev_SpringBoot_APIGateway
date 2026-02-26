package br.com.sebratel.bff.service.comercial;

import br.com.sebratel.bff.dto.comercial.PlanilhaInstalacaoDTO;
import br.com.sebratel.bff.repository.erp.comercial.RelatorioPlanilhaRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class RelatorioPlanilhaService {

    private static final Logger logger = LoggerFactory.getLogger(RelatorioPlanilhaService.class);

    private final RelatorioPlanilhaRepository repository;

    @Transactional(readOnly = true)
    public Stream<PlanilhaInstalacaoDTO> listarPlanilhaInstalacao(String nome) {
        logger.info("------- Listando Planilha Instalacao -------");
        return repository.findPlanilhaInstalacao(nome.trim().toUpperCase())
                .map(PlanilhaInstalacaoDTO::new);
    }
}