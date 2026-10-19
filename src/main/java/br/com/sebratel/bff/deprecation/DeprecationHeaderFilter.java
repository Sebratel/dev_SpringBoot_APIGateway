package br.com.sebratel.bff.deprecation;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Sinaliza rotas obsoletas ao consumidor (RFC 9745 e RFC 8594), sem alterar status nem corpo:
 * <ul>
 *   <li>{@code Deprecation: true}, ou {@code Deprecation: @epoch} quando {@code deprecation.since} esta definido;</li>
 *   <li>{@code Sunset: <data HTTP>} somente se {@code deprecation.sunset} estiver definido;</li>
 *   <li>{@code Link: <sucessora>; rel="successor-version"} quando o mesmo handler tem um alias em uso.</li>
 * </ul>
 * Cada chamada a uma rota obsoleta tambem gera uma linha no log DEPRECATED_ROUTE, para achar consumidores esquecidos.
 */
public class DeprecationHeaderFilter extends OncePerRequestFilter {

    private static final Logger AUDIT = LoggerFactory.getLogger("DEPRECATED_ROUTE");

    private final DeprecatedRoutes deprecatedRoutes;
    private final String deprecationValue;
    private final String sunsetValue;

    public DeprecationHeaderFilter(DeprecatedRoutes deprecatedRoutes, String since, String sunset) {
        this.deprecatedRoutes = deprecatedRoutes;
        this.deprecationValue = since.isBlank() ? "true" : "@" + startOfDayEpoch(since);
        this.sunsetValue = sunset.isBlank() ? null : httpDate(sunset);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Optional<DeprecatedRoutes.Match> match =
                deprecatedRoutes.find(request.getMethod(), pathWithinApplication(request));
        match.ifPresent(found -> {
            Optional<String> successor = found.route().successorFor(found.uriVariables());
            response.setHeader("Deprecation", deprecationValue);
            if (sunsetValue != null) {
                response.setHeader("Sunset", sunsetValue);
            }
            successor.ifPresent(link ->
                    response.addHeader(HttpHeaders.LINK, "<" + link + ">; rel=\"successor-version\""));
            AUDIT.info("method={} path={} successor={}", request.getMethod(), found.route().template(),
                    successor.orElse("-"));
        });
        chain.doFilter(request, response);
    }

    private static String pathWithinApplication(HttpServletRequest request) {
        String path = request.getRequestURI();
        String context = request.getContextPath();
        if (context != null && !context.isEmpty() && path.startsWith(context)) {
            path = path.substring(context.length());
        }
        return path.length() > 1 && path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

    private static long startOfDayEpoch(String isoDate) {
        return LocalDate.parse(isoDate).atStartOfDay().toEpochSecond(ZoneOffset.UTC);
    }

    private static String httpDate(String isoDate) {
        ZonedDateTime date = LocalDate.parse(isoDate).atStartOfDay(ZoneOffset.UTC);
        return DateTimeFormatter.RFC_1123_DATE_TIME.format(date);
    }
}
