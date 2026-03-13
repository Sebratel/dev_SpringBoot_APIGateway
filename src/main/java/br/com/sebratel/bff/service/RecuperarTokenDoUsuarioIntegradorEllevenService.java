package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.EllevenCredentialsDTO;
import br.com.sebratel.bff.dto.splitters.RecuperarTokenEllevenOutputDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class RecuperarTokenDoUsuarioIntegradorEllevenService {

    private final WebClient webClient;
    private final EllevenCredentialsDTO credentials;

    @Autowired
    public RecuperarTokenDoUsuarioIntegradorEllevenService(WebClient webClient, EllevenCredentialsDTO credentials) {
        this.webClient = webClient;
        this.credentials = credentials;
    }


    @Cacheable(value = "token-de-integracao", key = "'token-static-key'")
    public RecuperarTokenEllevenOutputDTO executar() {
        String ellevenTokenUrl = "https://erp.sebratel.net.br:45700/connect/token";
        return webClient.post()
                .uri(ellevenTokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(credentials.toFormData()))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> handleHttpError("conclusão", response))
                .bodyToMono(RecuperarTokenEllevenOutputDTO.class)
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
