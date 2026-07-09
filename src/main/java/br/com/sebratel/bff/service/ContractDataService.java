package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.RelatorioFinalDTO;
import br.com.sebratel.bff.repository.erp.ContractActivationRepository;
import br.com.sebratel.bff.repository.erp.projections.ContractActivationProjection;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

@Service
public class ContractDataService {

    private final ContractActivationRepository contractActivationRepository;

    public ContractDataService(ContractActivationRepository contractActivationRepository) {
        this.contractActivationRepository = contractActivationRepository;
    }

    @Cacheable(value = "contracts-activation", key = "'dados-completos'")
    @Transactional(readOnly = true)
    public List<RelatorioFinalDTO> getDadosCompletosCache() {
        return contractActivationRepository.findMergedContractData()
                .stream()
                .filter(distinctByKey(p -> p.getClientes() + p.getContrato()))
                .map(this::mapToDTO)
                .toList();
    }

    private RelatorioFinalDTO mapToDTO(ContractActivationProjection p) {
        String mesDescritivo = "";
        if (p.getDataCriacaoContrato() != null) {
            mesDescritivo = p.getDataCriacaoContrato()
                    .getMonth()
                    .getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
            mesDescritivo = mesDescritivo.substring(0, 1).toUpperCase() + mesDescritivo.substring(1);
        }

        return new RelatorioFinalDTO(
                p.getDataCriacaoContrato(),
                p.getDataAtivacao(),
                p.getContrato(),
                p.getVendedor(),
                p.getClientes(),
                p.getTecnologia(),
                p.getStatusContrato(),
                p.getStatusCancelamento(),
                mesDescritivo
        );
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}