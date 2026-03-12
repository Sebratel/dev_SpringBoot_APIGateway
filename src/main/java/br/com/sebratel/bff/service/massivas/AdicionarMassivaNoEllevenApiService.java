package br.com.sebratel.bff.service.massivas;

import br.com.sebratel.bff.dto.massivas.api.AberturaRegistroMassivoInputDTO;
import br.com.sebratel.bff.dto.massivas.api.AberturaRegistroMassivoOutputDTO;
import br.com.sebratel.bff.service.RecuperarTokenDoUsuarioIntegradorEllevenService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Objects;

@Service
@Slf4j
public class AdicionarMassivaNoEllevenApiService {

    private final RecuperarTokenDoUsuarioIntegradorEllevenService recuperarTokenDoUsuarioIntegradorEllevenService;
    private final WebClient webClient;
    private final CacheManager cacheManager;

    @Autowired
    public AdicionarMassivaNoEllevenApiService(RecuperarTokenDoUsuarioIntegradorEllevenService recuperarTokenDoUsuarioIntegradorEllevenService, WebClient webClient, CacheManager cacheManager) {
        this.recuperarTokenDoUsuarioIntegradorEllevenService = recuperarTokenDoUsuarioIntegradorEllevenService;
        this.webClient = webClient;
        this.cacheManager = cacheManager;
    }

    public AberturaRegistroMassivoOutputDTO executar(@Valid AberturaRegistroMassivoInputDTO input) {

        String token = recuperarTokenDoUsuarioIntegradorEllevenService.executar().accessToken();

        String url = "https://erp.sebratel.net.br:45715/external/integrations/thirdparty/opendetailedsolicitation";


        try {
            return webClient
                    .post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(input)
                    .retrieve()
                    .bodyToMono(AberturaRegistroMassivoOutputDTO.class)
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
