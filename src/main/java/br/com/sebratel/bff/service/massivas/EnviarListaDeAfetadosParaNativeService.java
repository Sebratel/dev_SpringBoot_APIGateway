package br.com.sebratel.bff.service.massivas;

import br.com.sebratel.bff.dto.ListaDeAfetadosDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class EnviarListaDeAfetadosParaNativeService {
    public ListaDeAfetadosDTO executar(ListaDeAfetadosDTO input) {
        WebClient webClient1 = WebClient.builder()
                .baseUrl("https://endpoint.native.com.br")
                .defaultHeader(HttpHeaders.ACCEPT, "*/*")
                .defaultHeader("Accept-Language", "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7")
                .build();

        return webClient1.post()
                .uri(uri -> uri.path("/rota-native").build())
                .bodyValue(input)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleHttpError)
                .bodyToMono(ListaDeAfetadosDTO.class)
                .block();
    }

    private Mono<? extends Throwable> handleHttpError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .flatMap(body -> {
                    log.error("Erro HTTP na etapa {}: Status {} - Body: {}", "conclusão", response.statusCode(), body);
                    return Mono.error(new RuntimeException("Falha na integração Elleven: " + body));
                });
    }
}
