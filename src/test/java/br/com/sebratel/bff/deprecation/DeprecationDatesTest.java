package br.com.sebratel.bff.deprecation;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Com as datas padrao do application.properties, o filtro emite Deprecation (@epoch) e Sunset (RFC 1123). */
class DeprecationDatesTest {

    @Test
    void defaultDatesAreTodayAndThreeMonthsLater() {
        new ApplicationContextRunner()
                .withUserConfiguration(DatesConfig.class)
                .withPropertyValues("deprecation.since=2026-10-17", "deprecation.sunset=2027-01-17")
                .run(context -> {
                    MockHttpServletResponse response = new MockHttpServletResponse();
                    context.getBean(DeprecationHeaderFilter.class).doFilter(
                            new MockHttpServletRequest("GET", "/api/v1/estoque/movimentacao"), response,
                            new MockFilterChain());

                    assertEquals("@1792195200", response.getHeader("Deprecation"));
                    assertEquals("Sun, 17 Jan 2027 00:00:00 GMT", response.getHeader("Sunset"));
                });
    }

    @Import(DeprecationConfig.class)
    static class DatesConfig {
    }
}
