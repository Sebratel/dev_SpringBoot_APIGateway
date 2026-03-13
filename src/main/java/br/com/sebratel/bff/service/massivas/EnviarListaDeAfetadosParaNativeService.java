package br.com.sebratel.bff.service.massivas;

import br.com.sebratel.bff.dto.massivas.ImpactedUsersDTO;
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
    public ImpactedUsersDTO executar(ImpactedUsersDTO input) {
       try{

       } catch (RuntimeException e) {
           throw new RuntimeException(e);
       }
        return input;
    }

    private Mono<? extends Throwable> handleHttpError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .flatMap(body -> {
                    log.error("Erro HTTP na etapa {}: Status {} - Body: {}", "conclusão", response.statusCode(), body);
                    return Mono.error(new RuntimeException("Falha na integração Elleven: " + body));
                });
    }
}
