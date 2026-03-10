package br.com.sebratel.bff.service.massivas;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

@Slf4j
@Component
public class RefreshTokenMassivasService {

    private final RestTemplate restTemplate;

    @Value("${app.external-api.url:www.url.com.br}")
    private String apiUrl;

    @Value("${app.external-api.token:defaultapitoken}")
    private String apiToken;

    public RefreshTokenMassivasService() {
        this.restTemplate = new RestTemplate();
    }

    public void fetchIncidentsTask() {



        log.info("Iniciando busca agendada de incidentes...");

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
        options.setCapability("goog:loggingPrefs", logPrefs);

        ChromeDriver driver = new ChromeDriver(options);
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> finalAuthHeader = new HashMap<>();

        try {
            driver.get("https://erp-staging.sebratel.net.br/ui/login");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            // 2. Fluxo de Login
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/div[1]/div/div[1]/div/div[2]/form/div[1]/div/div[2]/div/input")))
                    .sendKeys("03477920066");

            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/div[1]/div/div[1]/div/div[2]/form/div[2]/div/div[2]/div/input")))
                    .sendKeys("@YzwW9SiT9tia6c");

            driver.findElement(By.xpath("/html/body/div[1]/div/div[1]/div/div[2]/form/div[4]/button")).click();

            // 3. Navegação
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/div[1]/div/div[1]/div[1]/button"))).click();

            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/div[1]/div/div[1]/div[2]/div[1]/div[1]/div[2]/div/div/div/input")))
                    .sendKeys("solicita listagem");

            String href = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[1]/div/div[1]/div[2]/div[1]/div[2]/div/div/div[2]/a[2]")))
                    .getAttribute("href");

            assert href != null;
            driver.get(href);

            // Aguarda chamadas de rede
            Thread.sleep(20000);

            // 4. Captura e Processamento dos Logs
            LogEntries logs = driver.manage().logs().get(LogType.PERFORMANCE);
            System.out.println("Processando logs de rede...");

            for (LogEntry entry : logs) {
                try {
                    JsonNode messageNode = mapper.readTree(entry.getMessage()).get("message");
                    String method = messageNode.get("method").asText();

                    if ("Network.requestWillBeSent".equals(method)) {
                        JsonNode request = messageNode.get("params").get("request");
                        String url = request.get("url").asText();
                        JsonNode headers = request.get("headers");

                        // Filtro similar ao Python
                        if (url.contains("GetPrivacyLevelEnumForSelect")) {
                            // Procura por Authorization (case-insensitive)
                            headers.fieldNames().forEachRemaining(fieldName -> {
                                if (fieldName.equalsIgnoreCase("Authorization")) {
                                    finalAuthHeader.put("Authorization", headers.get(fieldName).asText());
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    // Log silencioso para mensagens que não seguem o padrão esperado
                }
            }

            // 5. Salva em arquivo JSON
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File("network_logs.json"), finalAuthHeader);
            System.out.println("Sucesso! Header salvo em network_logs.json");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
}