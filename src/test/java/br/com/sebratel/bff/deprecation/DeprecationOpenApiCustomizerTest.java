package br.com.sebratel.bff.deprecation;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeprecationOpenApiCustomizerTest {

    @Test
    void marksOnlyDeprecatedOperations() {
        Operation unused = new Operation();
        Operation inUse = new Operation().description("Matrix");
        Paths paths = new Paths();
        paths.addPathItem("/api/v1/estoque/movimentacao", new PathItem().get(unused));
        paths.addPathItem("/api/v1/matrix", new PathItem().get(inUse));
        OpenAPI openApi = new OpenAPI().paths(paths);

        new DeprecationOpenApiCustomizer(new DeprecatedRoutes()).customise(openApi);

        assertEquals(Boolean.TRUE, unused.getDeprecated());
        assertTrue(unused.getDescription().contains("Rota obsoleta"));
        assertNotEquals(Boolean.TRUE, inUse.getDeprecated());
        assertEquals("Matrix", inUse.getDescription());
    }

    @Test
    void methodMustMatch() {
        Operation post = new Operation();
        Paths paths = new Paths();
        paths.addPathItem("/api/v1/estoque/movimentacao", new PathItem().post(post));

        new DeprecationOpenApiCustomizer(new DeprecatedRoutes()).customise(new OpenAPI().paths(paths));

        assertNotEquals(Boolean.TRUE, post.getDeprecated());
    }
}
