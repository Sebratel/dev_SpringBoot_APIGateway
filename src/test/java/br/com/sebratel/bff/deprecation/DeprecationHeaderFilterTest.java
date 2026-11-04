package br.com.sebratel.bff.deprecation;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DeprecationHeaderFilterTest {

    private final DeprecatedRoutes routes = new DeprecatedRoutes();

    private MockHttpServletResponse call(DeprecationHeaderFilter filter, String method, String path)
            throws IOException, jakarta.servlet.ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }

    @Test
    void deprecatedRouteGetsDeprecationAndSuccessorLink() throws Exception {
        MockHttpServletResponse response = call(new DeprecationHeaderFilter(routes, "", ""),
                "GET", "/api/v1/usuario-afetado/contract/123");

        assertEquals("true", response.getHeader("Deprecation"));
        assertEquals("</api/v1/afetados/contract/123>; rel=\"successor-version\"", response.getHeader("Link"));
        assertNull(response.getHeader("Sunset"));
        assertEquals(200, response.getStatus());
    }

    @Test
    void deprecatedRouteWithoutSuccessorHasNoLink() throws Exception {
        MockHttpServletResponse response = call(new DeprecationHeaderFilter(routes, "", ""),
                "GET", "/api/v1/estoque/movimentacao");

        assertEquals("true", response.getHeader("Deprecation"));
        assertNull(response.getHeader("Link"));
    }

    @Test
    void routeInUseIsLeftUntouched() throws Exception {
        MockHttpServletResponse response = call(new DeprecationHeaderFilter(routes, "", ""),
                "GET", "/api/v1/matrix");

        assertNull(response.getHeader("Deprecation"));
        assertNull(response.getHeader("Link"));
    }

    @Test
    void trailingSlashStillMatches() throws Exception {
        MockHttpServletResponse response = call(new DeprecationHeaderFilter(routes, "", ""),
                "GET", "/api/v1/estoque/movimentacao/");

        assertEquals("true", response.getHeader("Deprecation"));
    }

    @Test
    void sinceAndSunsetPropertiesUseTheRfcFormats() throws Exception {
        MockHttpServletResponse response = call(new DeprecationHeaderFilter(routes, "2026-10-20", "2027-01-31"),
                "GET", "/api/v1/estoque/movimentacao");

        assertEquals("@1792454400", response.getHeader("Deprecation"));
        assertEquals("Sun, 31 Jan 2027 00:00:00 GMT", response.getHeader("Sunset"));
    }
}
