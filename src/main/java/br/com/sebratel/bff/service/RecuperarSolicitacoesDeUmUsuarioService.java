package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.splitters.RecuperarSolicitacaoDeClienteOutputDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Objects;

@Service
@Slf4j
public class RecuperarSolicitacoesDeUmUsuarioService {

    private final WebClient webClient;
    private final CacheManager cacheManager;
    private final RecuperarTokenDoUsuarioIntegradorEllevenService recuperarTokenDoUsuarioIntegradorEllevenService;


    @Autowired
    public RecuperarSolicitacoesDeUmUsuarioService(WebClient webClient, CacheManager cacheManager, RecuperarTokenDoUsuarioIntegradorEllevenService recuperarTokenDoUsuarioIntegradorEllevenService) {
        this.webClient = webClient;
        this.cacheManager = cacheManager;
        this.recuperarTokenDoUsuarioIntegradorEllevenService = recuperarTokenDoUsuarioIntegradorEllevenService;
    }

    public RecuperarSolicitacaoDeClienteOutputDTO executar(String clientId) {

        String token = recuperarTokenDoUsuarioIntegradorEllevenService.executar().accessToken();
        String url = "https://erp.sebratel.net.br:45715/external/integrations/thirdparty/solicitationlist/" + clientId + "?allAssignments=false";
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(100 * 1024 * 1024))
                .build();

        try {
            return webClient.mutate()
                    .exchangeStrategies(strategies)
                    .build()
                    .get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .bodyToMono(RecuperarSolicitacaoDeClienteOutputDTO.class)
                    .block();

        } catch (WebClientResponseException.Unauthorized e) {
            if (cacheManager.getCache("token-de-integracao") != null) {
                Objects.requireNonNull(cacheManager.getCache("token-de-integracao")).evict("token-static-key");
                log.error("Cache do token invalidado devido a erro 401 na API.");
            }
            throw e;
        }
    }
}
