package br.com.sebratel.bff;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.TimeZone;

@Slf4j
@SpringBootApplication
@EnableCaching
@EnableScheduling
public class BffApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
		loadEnv();
		SpringApplication.run(BffApplication.class, args);
	}

	private static void loadEnv() {
		// Tenta encontrar o arquivo na raiz do projeto
		File envFile = new File(".env");

		System.out.println(">>> Procurando arquivo .env em: " + envFile.getAbsolutePath());

		if (envFile.exists()) {
			try {
				List<String> lines = Files.readAllLines(envFile.toPath());
				for (String line : lines) {
					line = line.trim();
					if (line.isEmpty() || line.startsWith("#")) continue;

					String[] parts = line.split("=", 2);
					if (parts.length == 2) {
						String key = parts[0].trim();
						String value = parts[1].trim();
						System.setProperty(key, value);
					}
				}
				System.out.println(">>> Arquivo .env carregado com sucesso!");
			} catch (Exception e) {
				System.err.println(">>> Erro ao ler o arquivo .env: " + e.getMessage());
			}
		} else {
			System.out.println(">>> AVISO: Arquivo .env não encontrado no caminho acima!");
		}

	}

}
