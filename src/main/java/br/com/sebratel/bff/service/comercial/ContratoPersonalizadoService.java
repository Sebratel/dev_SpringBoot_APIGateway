package br.com.sebratel.bff.service.comercial;

import br.com.sebratel.bff.dto.comercial.ContratoPersonalizadoDTO;
import br.com.sebratel.bff.repository.erp.comercial.ContratoPersonalizadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ContratoPersonalizadoService {

    private final ContratoPersonalizadoRepository repository;

    @Transactional(readOnly = true)
    public Stream<ContratoPersonalizadoDTO> listarContratosPersonalizados(LocalDateTime inicio, LocalDateTime fim, List<String> listaDeClientes) {
        return repository.findContratosPersonalizados(inicio, fim, listaDeClientes)
                .stream()
                .map(ContratoPersonalizadoDTO::new);
    }
}