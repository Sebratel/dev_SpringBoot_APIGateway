package br.com.sebratel.bff.service.massivas;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Slf4j
@Component
public class RefreshTokenMassivasService {

    private final RestTemplate restTemplate;

    @Value("${app.external-api.url}")
    private String apiUrl;

    @Value("${app.external-api.token}")
    private String apiToken;

    public RefreshTokenMassivasService() {
        this.restTemplate = new RestTemplate();
    }

    // Roda a cada 2 horas e 30 minutos
    @Scheduled(fixedRateString = "${app.schedule.intervalo}")
    public void fetchIncidentsTask() {
        log.info("Iniciando busca agendada de incidentes...");

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.ALL));
            headers.add("Accept-Language", "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7");
            headers.setBearerAuth(apiToken);
            headers.add("Connection", "keep-alive");
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Origin", "https://erp.sebratel.net.br");
            headers.add("Referer", "https://erp.sebratel.net.br/");
            headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36");
            headers.add("sec-ch-ua", "\"Not:A-Brand\";v=\"99\", \"Google Chrome\";v=\"145\", \"Chromium\";v=\"145\"");
            headers.add("sec-ch-ua-mobile", "?0");
            headers.add("sec-ch-ua-platform", "\"Windows\"");

            HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Dados obtidos com sucesso!");
                // Aqui você chamaria seu Service para processar o response.getBody()
            }

        } catch (Exception e) {
            log.error("Erro ao executar a chamada agendada: {}", e.getMessage());
        }
    }
}