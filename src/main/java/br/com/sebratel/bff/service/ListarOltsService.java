package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.splitters.EllevenSplitterResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class ListarOltsService {
    private final WebClient webClient;

    @Autowired
    public ListarOltsService(WebClient webClient) {
        this.webClient = webClient;
    }

    public EllevenSplitterResponseDTO executar(String token) {
        String url = "https://erp.sebratel.net.br:45715/external/map/olt/all";
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(100 * 1024 * 1024))
                .build();

        return webClient.mutate()
                .exchangeStrategies(strategies)
                .build()
                .get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(EllevenSplitterResponseDTO.class)
                .block();
    }
}
