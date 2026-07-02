package br.com.sebratel.bff.service;

import br.com.sebratel.bff.exceptions.ResourceNotFoundException;
import br.com.sebratel.bff.model.Employee;
import br.com.sebratel.bff.repository.erp.EmployeeRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public Employee getPersonByCPF(@Valid @NotNull String txId) {
        log.info("Buscando PersonId para o email: {}", txId);
        return employeeRepository.findByTxId(txId);
    }
}
