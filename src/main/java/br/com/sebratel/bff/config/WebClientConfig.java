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

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("https://erp-staging.sebratel.net.br")
                .defaultHeader(HttpHeaders.ACCEPT, "*/*")
                .defaultHeader("Accept-Language", "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7")
                .defaultHeader("Connection", "keep-alive")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded; charset=UTF-8")
                .defaultHeader("Origin", "https://erp-staging.sebratel.net.br")
                .defaultHeader("Referer", "https://erp-staging.sebratel.net.br/network_maintenances")
                .defaultHeader("Sec-Fetch-Dest", "empty")
                .defaultHeader("Sec-Fetch-Mode", "cors")
                .defaultHeader("Sec-Fetch-Site", "same-origin")
                .defaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36")
                .defaultHeader("X-Requested-With", "XMLHttpRequest")
                .defaultHeader("sec-ch-ua", "\"Not:A-Brand\";v=\"99\", \"Google Chrome\";v=\"145\", \"Chromium\";v=\"145\"")
                .defaultHeader("sec-ch-ua-mobile", "?0")
                .defaultHeader("sec-ch-ua-platform", "\"Windows\"")
                .defaultHeader(HttpHeaders.COOKIE, "_hjSessionUser_5073910=eyJpZCI6IjIzNTcyMGQ2LTVjOTMtNTUzMi05NGU1LWU1NWQ0YzJkZTE1OCIsImNyZWF0ZWQiOjE3NzI2MjkxMjMzNTQsImV4aXN0aW5nIjp0cnVlfQ==; _hjSession_5073910=eyJpZCI6IjYyOGEyMjM4LTVjZDgtNGRjYi1hMDVkLTEwYzlkZGZlZWU1ZiIsImMiOjE3NzI2MzU1ODcwNTUsInMiOjAsInIiOjAsInNiIjowLCJzciI6MCwic2UiOjAsImZzIjowLCJzcCI6MX0=; SYNSUITE=p0e98ioh96a1f3u30d68q9ugi0")
                .build();
    }
}