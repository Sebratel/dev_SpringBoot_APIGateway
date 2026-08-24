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
        return employeeRepository.findPersonIdByEmail(email).orElseThrow(() -> new ResourceNotFoundException("PersonId não encontrado com o email fornecido: " + email ));
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
