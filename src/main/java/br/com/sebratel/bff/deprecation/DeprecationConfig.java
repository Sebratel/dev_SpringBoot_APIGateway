package br.com.sebratel.bff.deprecation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra os beans de deprecacao de rotas. Fica em uma {@code @Configuration} (e nao em {@code @Component}) de
 * proposito: os testes de fatia {@code @WebMvcTest} carregam todo {@code Filter} detectado por varredura, mas nao as
 * suas dependencias, e quebrariam ao subir.
 */
@Configuration
public class DeprecationConfig {

    @Bean
    public DeprecatedRoutes deprecatedRoutes() {
        return new DeprecatedRoutes();
    }

    @Bean
    public DeprecationHeaderFilter deprecationHeaderFilter(DeprecatedRoutes deprecatedRoutes,
                                                           @Value("${deprecation.since:}") String since,
                                                           @Value("${deprecation.sunset:}") String sunset) {
        return new DeprecationHeaderFilter(deprecatedRoutes, since, sunset);
    }

    @Bean
    public DeprecationOpenApiCustomizer deprecationOpenApiCustomizer(DeprecatedRoutes deprecatedRoutes) {
        return new DeprecationOpenApiCustomizer(deprecatedRoutes);
    }
}
