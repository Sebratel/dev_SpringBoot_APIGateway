package br.com.sebratel.bff.config;

import br.com.sebratel.bff.dto.TokenResponseDTO;
import lombok.Getter;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Getter
@Component
public class OAuth2Filter {

    private String cachedToken;

    public ExchangeFilterFunction authFilter() {
        return (request, next) -> {
            // Se já temos o token, adicionamos no header Authorization
            if (cachedToken != null) {
                request = org.springframework.web.reactive.function.client.ClientRequest.from(request)
                        .header("Authorization", "Bearer " + cachedToken)
                        .build();
            }
            return next.exchange(request);
        };
    }

    public Mono<String> renewToken() {
        WebClient authClient = WebClient.builder()
                .baseUrl("https://erp.sebratel.net.br:45700")
                .build();

        MultiValueMap<String, String> formData = getInformation();

        return authClient.post()
                .uri("/connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(TokenResponseDTO.class)
                .map(res -> {
                    this.cachedToken = res.getAccessToken();
                    return this.cachedToken;
                });
    }



    // TODO: remover esse código assim que possível.
    private static @NonNull MultiValueMap<String, String> getInformation() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");
        formData.add("scope", "syngw");
        formData.add("client_id", "ad0c5d9a-fad1-4ca9-8d1e-cff2cedb3146");
        formData.add("client_secret", "cb53bd13-5305-4306-b03b-b00cf05f2e34");
        formData.add("syndata", "TWpNMU9EYzVaakk1T0dSaU1USmxaalprWldFd00ySTFZV1JsTTJRMFptUT06WlhsS1ZHVlhOVWxpTTA0d1NXcHZhVTFVWnpKTWFrbDRUMU0wZUUxcVozVk5hbFY0U1dsM2FWVXpiSFZTUjBscFQybEthMWx0Vm5SalJFRjNUVlJCZDBscGQybFNSMHBWWlZoQ2JFbHFiMmxqUnpsNlpFZGtlVnBZVFdsbVVUMDk6WlRoa01qTTFZamswWXpsaU5ETm1aRGczTURsa01qWTJZekF4TUdNM01HVT0=");
        return formData;
    }
}