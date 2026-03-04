package br.com.sebratel.bff.config;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.contrib.json.classic.JsonLayout;
import ch.qos.logback.contrib.jackson.JacksonJsonFormatter;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class LoggerConfig {

    @PostConstruct
    public void configureLogs() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        // 1. Criar o Appender de Console
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(context);
        consoleAppender.setName("CONSOLE_JSON");

        // 2. Configurar o Layout JSON (Onde os campos são definidos)
        JsonLayout jsonLayout = new JsonLayout();
        jsonLayout.setContext(context);
        jsonLayout.setTimestampFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        jsonLayout.setIncludeMDC(true); // Essencial para rastrear ID de Massiva
        jsonLayout.setIncludeContextName(true);

        // Formatador para transformar o mapa em string JSON usando Jackson
        JacksonJsonFormatter formatter = new JacksonJsonFormatter();
        jsonLayout.setJsonFormatter(formatter);
        jsonLayout.start();

        // 3. ENCODER: Esta é a correção para o erro que apareceu no log
        // O Logback moderno exige que o layout seja "embrulhado" por um encoder
        LayoutWrappingEncoder<ILoggingEvent> encoder = new LayoutWrappingEncoder<>();
        encoder.setContext(context);
        encoder.setLayout(jsonLayout);
        encoder.start();

        // 4. Acoplar o encoder ao appender em vez do layout direto
        consoleAppender.setEncoder(encoder);
        consoleAppender.start();

        // 5. Atualizar o Logger ROOT
        ch.qos.logback.classic.Logger rootLogger = context.getLogger("ROOT");
        rootLogger.detachAndStopAllAppenders();
        rootLogger.addAppender(consoleAppender);
    }
}