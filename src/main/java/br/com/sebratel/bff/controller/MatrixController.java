package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.ApiResponse;
import br.com.sebratel.bff.dto.ClienteMassivaPorTelefoneOutputDTO;
import br.com.sebratel.bff.dto.MatrixMassiveInputDTO;
import br.com.sebratel.bff.dto.MatrixMassiveOutputDTO;
import br.com.sebratel.bff.dto.MatrixTelefoneInputDTO;
import br.com.sebratel.bff.service.ClienteMassivaPorTelefoneService;
import br.com.sebratel.bff.service.MatrixService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/matrix")
public class MatrixController {

    final MatrixService matrixService;
    final ClienteMassivaPorTelefoneService clienteMassivaPorTelefoneService;

    @Autowired
    public MatrixController(MatrixService matrixService,
                            ClienteMassivaPorTelefoneService clienteMassivaPorTelefoneService) {
        this.matrixService = matrixService;
        this.clienteMassivaPorTelefoneService = clienteMassivaPorTelefoneService;
    }

    /**
     * Situacao de massiva de quem ligou, a partir do telefone (alerta ao atendente). Exige autenticacao, ao
     * contrario do GET por CPF: devolve se um telefone pertence a um cliente e o motivo da massiva dele.
     */
    @PostMapping("/telefone")
    public ApiResponse<ClienteMassivaPorTelefoneOutputDTO> getMassiveInfoByPhone(
            @Valid @RequestBody MatrixTelefoneInputDTO input) {
        return ApiResponse.<ClienteMassivaPorTelefoneOutputDTO>builder()
                .success(true)
                .message("Massive data by phone retrieved successfully")
                .data(clienteMassivaPorTelefoneService.consultar(input.getTelefone()))
                .build();
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
