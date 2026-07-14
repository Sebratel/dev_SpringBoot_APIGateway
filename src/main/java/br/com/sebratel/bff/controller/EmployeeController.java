package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.ApiResponse;
import br.com.sebratel.bff.model.Employee;
import br.com.sebratel.bff.service.EmployeeService;
import br.com.sebratel.bff.utils.JwtInformation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employee")
@Slf4j
public class EmployeeController {

    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/get-user-by-token")
    ApiResponse<Employee> getUserByToken() {
        log.info("Recebida requisição para obter informações do usuário pelo token.");
        return ApiResponse.<Employee>builder()
                .message("Sucesso")
                .success(true)
                .data(JwtInformation.retrieveUserData())
                .build();
    }

    @GetMapping("/get-person-id-by-email")
    ApiResponse<Long> getPersonIdByEmail(@RequestParam @Valid @NotNull String email) {
        log.info("Recebida requisição para obter PersonId pelo email: {}", email);
        return ApiResponse.<Long>builder()
                .success(true)
                .message("Sucesso")
                .data(employeeService.getPersonIdByEmail(email))
                .build();
    }

    @GetMapping("/get-person-name-by-cpf")
    ApiResponse<String> getPersonByCPF(@RequestParam @Valid @NotNull String txId) {
        log.info("Recebida requisição para obter Person pelo cpf: {}", txId);
        return ApiResponse.<String>builder()
                .success(true)
                .message("Sucesso")
                .data(employeeService.getPersonByCPF(txId))
                .build();
    }

    @GetMapping("/get-cpf-by-contract")
    ApiResponse<Long> getCpfByContract(@RequestParam @Valid @NotNull Long contract) {
        log.info("Recebida requisição para obter Person pelo cpf: {}", contract);
        return ApiResponse.<Long>builder()
                .success(true)
                .message("Sucesso")
                .data(employeeService.getTxIdByContract(contract))
                .build();
    }

}
