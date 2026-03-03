package br.com.sebratel.bff.config;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
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

        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(context);
        consoleAppender.setName("CONSOLE_JSON");

        JsonLayout jsonLayout = new JsonLayout();
        jsonLayout.setContext(context);
        jsonLayout.setTimestampFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        jsonLayout.setIncludeMDC(true);
        jsonLayout.setIncludeContextName(true);

        JacksonJsonFormatter formatter = new JacksonJsonFormatter();
        jsonLayout.setJsonFormatter(formatter);

        jsonLayout.start();
        consoleAppender.setLayout(jsonLayout);
        consoleAppender.start();

        ch.qos.logback.classic.Logger rootLogger = context.getLogger("ROOT");
        rootLogger.detachAndStopAllAppenders();
        rootLogger.addAppender(consoleAppender);
    }
}