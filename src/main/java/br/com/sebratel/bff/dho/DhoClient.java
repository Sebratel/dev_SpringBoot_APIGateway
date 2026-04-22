package br.com.sebratel.bff.dho;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class DhoClient {

    private final WebClient webClient;

    public DhoClient(WebClient.Builder webClientBuilder, @Value("${dho.api.url}") String dhoApiUrl) {
        this.webClient = webClientBuilder.baseUrl(dhoApiUrl).build();
    }

    public Mono<Object> get(String path) {
        log.info("DHO Client GET: {}", path);
        return webClient.get()
                .uri(path)
                .retrieve()
                .bodyToMono(Object.class)
                .doOnSuccess(response -> log.info("DHO Client Response for GET {}: {}", path, response))
                .doOnError(error -> log.error("DHO Client Error for GET {}: {}", path, error.getMessage()));
    }

    public Mono<Object> post(String path, Object body) {
        log.info("DHO Client POST: {} with body: {}", path, body);
        return webClient.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Object.class)
                .doOnSuccess(response -> log.info("DHO Client Response for POST {}: {}", path, response))
                .doOnError(error -> log.error("DHO Client Error for POST {}: {}", path, error.getMessage()));
    }

    public Mono<Object> put(String path, Object body) {
        log.info("DHO Client PUT: {} with body: {}", path, body);
        return webClient.put()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Object.class)
                .doOnSuccess(response -> log.info("DHO Client Response for PUT {}: {}", path, response))
                .doOnError(error -> log.error("DHO Client Error for PUT {}: {}", path, error.getMessage()));
    }

    public Mono<Void> delete(String path) {
        log.info("DHO Client DELETE: {}", path);
        return webClient.delete()
                .uri(path)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(v -> log.info("DHO Client DELETE successful for: {}", path))
                .doOnError(error -> log.error("DHO Client Error for DELETE {}: {}", path, error.getMessage()));
    }
}
