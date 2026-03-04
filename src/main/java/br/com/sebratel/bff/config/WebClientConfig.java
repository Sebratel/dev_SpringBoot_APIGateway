package br.com.sebratel.bff.config;

import br.com.sebratel.bff.enums.VoalleHeaderEnums;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class WebClientConfig {

    private final OAuth2Filter oAuth2Filter;

    public WebClientConfig(OAuth2Filter oAuth2Filter) {
        this.oAuth2Filter = oAuth2Filter;
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("https://erp-staging.sebratel.net.br")
                // Headers fixos de compatibilidade extraídos do seu CURL
                .defaultHeader(HttpHeaders.ACCEPT, "*/*")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .defaultHeader(VoalleHeaderEnums.X_REQUESTED_WITH, VoalleHeaderEnums.XML_HTTP_REQUEST)
                .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36")
                .defaultHeader(HttpHeaders.COOKIE, "SYNSUITE=pbepp6bellbe7at5rhh44m5rq1")
                .defaultHeader(HttpHeaders.ORIGIN, "https://erp-staging.sebratel.net.br")
                .defaultHeader(HttpHeaders.REFERER, "https://erp-staging.sebratel.net.br/network_maintenances")

                .filter(addBearerToken())
                .filter(retryOnUnauthorized())

                .build();
    }

    /**
     * Interceptor que adiciona o Header Authorization em cada chamada
     */
    private ExchangeFilterFunction addBearerToken() {
        return (request, next) -> {
            String token = oAuth2Filter.getCachedToken();
            if (token != null) {
                request = ClientRequest.from(request)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .build();
            }
            return next.exchange(request);
        };
    }

    /**
     * Interceptor de resiliência: Se receber 401, renova o token e tenta de novo uma vez.
     */
    private ExchangeFilterFunction retryOnUnauthorized() {
        return (request, next) -> next.exchange(request)
                .flatMap(response -> {
                    if (response.statusCode() == HttpStatus.UNAUTHORIZED) {
                        return oAuth2Filter.renewToken()
                                .flatMap(newToken -> {
                                    ClientRequest retryRequest = ClientRequest.from(request)
                                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + newToken)
                                            .build();
                                    return next.exchange(retryRequest);
                                });
                    }
                    return Mono.just(response);
                });
    }
}