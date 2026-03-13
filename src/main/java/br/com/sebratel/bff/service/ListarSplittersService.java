package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.splitters.EllevenSplitterResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class ListarSplittersService {

    private final WebClient webClient;

    @Autowired
    public ListarSplittersService(WebClient webClient) {
        this.webClient = webClient;
    }

    public EllevenSplitterResponseDTO executar(String token) {
        String url = "https://erp.sebratel.net.br:45715/external/map/splitter/all";
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


    private Mono<? extends Throwable> handleHttpError(String context, ClientResponse response) {
        return response.bodyToMono(String.class)
                .flatMap(body -> {
                    log.error("Erro HTTP na etapa {}: Status {} - Body: {}", context, response.statusCode(), body);
                    return Mono.error(new RuntimeException("Falha na integração Elleven: " + body));
                });
    }
}
