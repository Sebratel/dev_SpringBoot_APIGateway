package br.com.sebratel.bff.deprecation;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import org.springdoc.core.customizers.OpenApiCustomizer;

/** Marca como {@code deprecated} no OpenAPI (Swagger UI) cada operacao que esta em deprecated-routes.txt. */
public class DeprecationOpenApiCustomizer implements OpenApiCustomizer {

    private static final String NOTE =
            "Rota obsoleta: sem nenhuma requisicao nos ultimos meses. Responde com o header Deprecation.";

    private final DeprecatedRoutes deprecatedRoutes;

    public DeprecationOpenApiCustomizer(DeprecatedRoutes deprecatedRoutes) {
        this.deprecatedRoutes = deprecatedRoutes;
    }

    @Override
    public void customise(OpenAPI openApi) {
        if (openApi.getPaths() == null) {
            return;
        }
        openApi.getPaths().forEach((path, item) -> item.readOperationsMap().forEach((method, operation) -> {
            if (deprecatedRoutes.isDeprecatedTemplate(method.name(), path)) {
                mark(operation);
            }
        }));
    }

    private static void mark(Operation operation) {
        operation.setDeprecated(true);
        String description = operation.getDescription();
        operation.setDescription(description == null || description.isBlank() ? NOTE : description + "\n\n" + NOTE);
    }
}
