package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.RelatorioFinalDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.stream.Stream;

@Service
public class ApoioSemanalService {

    private final ContractDataService contractDataService;

    @Autowired
    public ApoioSemanalService(ContractDataService contractDataService) {
        this.contractDataService = contractDataService;
    }

    @Transactional(readOnly = true)
    public Stream<RelatorioFinalDTO> streamRelatorioPorVendedor(String nomeVendedor) {
        String mesAtual = LocalDate.now()
                .getMonth()
                .getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
        mesAtual = mesAtual.substring(0, 1).toUpperCase() + mesAtual.substring(1);

        final String vendedorTarget = nomeVendedor.trim().toUpperCase();
        final String mesTarget = mesAtual;

        return contractDataService.getDadosCompletosCache().stream()
                .filter(dto -> {
                    boolean mesmoVendedor = dto.vendedor() != null &&
                            dto.vendedor().trim().toUpperCase().equals(vendedorTarget);
                    boolean mesmoMes = mesTarget.equals(dto.mesDaCriacao());

                    return mesmoVendedor && mesmoMes;
                });
    }
}