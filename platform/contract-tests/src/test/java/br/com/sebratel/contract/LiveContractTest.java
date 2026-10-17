package br.com.sebratel.contract;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercita as 150 rotas do manifesto contra uma URL base: o monolito hoje, o gateway depois.
 * O MESMO teste roda nos dois; a paridade e o criterio de aceite da migracao.
 *
 * <p>Variaveis de ambiente:
 * <ul>
 *   <li>{@code CONTRACT_BASE_URL}: obrigatoria; sem ela o teste e ignorado (ex.: {@code http://localhost:8085}).</li>
 *   <li>{@code CONTRACT_TOKEN}: ID token Google valido. Sem ele, so a checagem de autenticacao (401) roda.</li>
 *   <li>{@code CONTRACT_RECORD=true}: grava as respostas em {@code golden/} em vez de comparar.</li>
 *   <li>{@code CONTRACT_GOLDEN_DIR}: pasta dos golden files (padrao {@code golden}).</li>
 * </ul>
 *
 * <p>Seguranca: rotas que alteram dados ({@code safe=false}) NUNCA sao chamadas autenticadas. Para elas so se
 * confere que a chamada sem token e recusada com 401, o que nao tem efeito colateral.
 */
@EnabledIfEnvironmentVariable(named = "CONTRACT_BASE_URL", matches = ".+")
class LiveContractTest {

    /** Status que indicam rota nao encaminhada ou serviço fora do ar, nunca uma resposta de negocio. */
    private static final Set<Integer> ROUTING_FAILURES = Set.of(405, 502, 503, 504);

    private final String baseUrl = System.getenv("CONTRACT_BASE_URL").replaceAll("/+$", "");
    private final String token = System.getenv("CONTRACT_TOKEN");
    private final boolean record = "true".equalsIgnoreCase(System.getenv("CONTRACT_RECORD"));
    private final GoldenStore goldens = new GoldenStore(
            Path.of(Optional.ofNullable(System.getenv("CONTRACT_GOLDEN_DIR")).orElse("golden")));
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final List<Route> routes = ManifestLoader.load();

    @TestFactory
    Stream<DynamicTest> unauthenticatedCallsAreRejected() {
        return routes.stream().filter(route -> !route.isPublic()).map(route ->
                DynamicTest.dynamicTest(route.label(), () -> {
                    HttpResponse<String> response = call(route, null);
                    assertEquals(401, response.statusCode(), "rota protegida deve responder 401 sem token");
                }));
    }

    @TestFactory
    Stream<DynamicTest> publicRoutesAreReachableWithoutToken() {
        return routes.stream().filter(Route::isPublic).map(route ->
                DynamicTest.dynamicTest(route.label(), () -> {
                    HttpResponse<String> response = call(route, null);
                    assertNotEquals(401, response.statusCode());
                    assertNotEquals(403, response.statusCode());
                    assertFalse(ROUTING_FAILURES.contains(response.statusCode()), "status " + response.statusCode());
                }));
    }

    @TestFactory
    Stream<DynamicTest> authenticatedSafeCallsMatchTheGoldenFiles() {
        if (token == null || token.isBlank()) {
            return Stream.empty();
        }
        return routes.stream().filter(Route::safe).map(route ->
                DynamicTest.dynamicTest(route.label(), () -> {
                    HttpResponse<String> response = call(route, token);
                    assertFalse(ROUTING_FAILURES.contains(response.statusCode()),
                            "rota nao encaminhada ou servico indisponivel: " + response.statusCode());
                    Golden actual = new Golden(response.statusCode(), List.copyOf(ShapeExtractor.of(response.body())));
                    if (record) {
                        goldens.write(route, actual);
                        return;
                    }
                    Optional<Golden> expected = goldens.read(route);
                    if (expected.isPresent()) {
                        assertEquals(expected.get(), actual, "resposta divergente do golden file");
                    }
                }));
    }

    private HttpResponse<String> call(Route route, String bearer) throws IOException, InterruptedException {
        HttpRequest.BodyPublisher body = route.hasBody()
                ? HttpRequest.BodyPublishers.ofString(SampleValues.body(route))
                : HttpRequest.BodyPublishers.noBody();
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(baseUrl + SampleValues.path(route)))
                .timeout(Duration.ofSeconds(120))
                .header("Accept", "application/json")
                .method(route.method(), body);
        if (route.hasBody()) {
            request.header("Content-Type", "application/json");
        }
        if (bearer != null) {
            request.header("Authorization", "Bearer " + bearer);
        }
        assertTrue(route.path().startsWith("/"), route.label());
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }
}
