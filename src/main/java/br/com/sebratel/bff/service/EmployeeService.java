package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.CorporativoOutputDTO;
import br.com.sebratel.bff.dto.InsigniaOutputDTO;
import br.com.sebratel.bff.exceptions.InsigniaNotFoundException;
import br.com.sebratel.bff.exceptions.ResourceNotFoundException;
import br.com.sebratel.bff.repository.erp.EmployeeRepository;
import br.com.sebratel.bff.repository.erp.projections.InsigniaProjection;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public Long getPersonIdByEmail(String email) {
        log.info("Buscando PersonId para o email: {}", email);
        // A query já vem ordenada (cadastro com CPF primeiro, depois menor id). Deduplicamos
        // preservando essa ordem — o join pode repetir o mesmo id quando v_users tem a linha
        // duplicada, e nesse caso NÃO é ambiguidade real de pessoa.
        List<Long> ids = employeeRepository.findPersonIdsByEmail(email).stream().distinct().toList();
        if (ids.isEmpty()) {
            throw new ResourceNotFoundException("PersonId não encontrado com o email fornecido: " + email);
        }
        if (ids.size() > 1) {
            // Cadastro duplicado no ERP: o mesmo e-mail tem mais de uma pessoa DISTINTA. Não
            // derrubamos a abertura (evita HTTP 500) — usamos o primeiro da ordem (cadastro com
            // CPF / mais antigo) e registramos o aviso para o duplicado ser tratado no Voalle.
            log.warn("E-mail com mais de um PersonId no ERP (cadastro duplicado): {} -> {}. Usando: {}.",
                    email, ids, ids.getFirst());
        }
        return ids.getFirst();
    }

    public boolean hasB2BinInput(List<Long> list) {
        return employeeRepository.hasB2BinInput(list);
    }

    private static final Set<String> INSIGNIAS_CORPORATIVAS = Set.of("Contrato Corporativo PME", "Contrato Corporativo");

    public CorporativoOutputDTO getCorporativoByTxId(@Valid @NotNull String txId) {
        log.info("Verificando se o cliente é corporativo para o cpf/cnpj: {}", txId);

        if (employeeRepository.findByTxId(txId).isEmpty()) {
            throw new ResourceNotFoundException("Cliente não encontrado para o cpf/cnpj fornecido: " + txId);
        }

        InsigniaProjection insigniaProjection = employeeRepository.findInsigniaByTxId(txId)
                .orElseThrow(() -> new InsigniaNotFoundException("Cliente encontrado, porém sem insígnia cadastrada para o cpf/cnpj: " + txId));

        InsigniaOutputDTO insignia = InsigniaOutputDTO.fromProjection(insigniaProjection);
        boolean corporativo = INSIGNIAS_CORPORATIVAS.contains(insignia.getTitle());
        return new CorporativoOutputDTO(insignia, corporativo);
    }

    public String getPersonByCPF(@Valid @NotNull String txId) {
        log.info("Buscando PersonId para o cpf: {}", txId);
        return employeeRepository.findByTxId(txId).getFirst();
    }

    public String getTxIdByContract(@Valid @NotNull Long contract) {
        log.info("Buscando txId para o contrato: {}", contract);
        return employeeRepository.findTxIdByContract(contract);
    }
}
