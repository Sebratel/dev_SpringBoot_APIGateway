package br.com.sebratel.bff.service;

import br.com.sebratel.bff.exceptions.ResourceNotFoundException;
import br.com.sebratel.bff.repository.erp.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

}
