package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.ApiResponse;
import br.com.sebratel.bff.dto.MatrixMassiveInputDTO;
import br.com.sebratel.bff.dto.MatrixMassiveOutputDTO;
import br.com.sebratel.bff.service.MatrixService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/matrix")
public class MatrixController {

    final MatrixService matrixService;

    @Autowired
    public MatrixController(MatrixService matrixService) {
        this.matrixService = matrixService;
    }


    @GetMapping
    public ApiResponse<MatrixMassiveOutputDTO> getMassiveInfo(@RequestBody MatrixMassiveInputDTO inputDTO) {
        return ApiResponse.<MatrixMassiveOutputDTO>builder()
                .success(true)
                .message("Massive data retrived sucessfully")
                .data(matrixService.getContractInfoByCPF(inputDTO.getCpf()))
                .build();
    }
}
