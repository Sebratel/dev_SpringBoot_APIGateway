package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.ApiResponse;
import br.com.sebratel.bff.model.Employee;
import br.com.sebratel.bff.service.EmployeeService;
import br.com.sebratel.bff.utils.JwtInformation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
