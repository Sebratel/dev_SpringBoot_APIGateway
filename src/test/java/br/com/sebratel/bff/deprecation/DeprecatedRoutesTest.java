package br.com.sebratel.bff.deprecation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guarda de deprecated-routes.txt: a lista tem de ser EXATAMENTE o conjunto de rotas do manifesto com 0 requisicoes
 * em docs/uso-endpoints-elk.csv. Rota que ganha trafego nao pode ficar obsoleta; rota nova sem uso nao some da lista.
 */
class DeprecatedRoutesTest {

    private static final Path USAGE = Path.of("docs", "uso-endpoints-elk.csv");
    private static final Path MANIFEST = Path.of("docs", "contract", "manifest.json");

    private final DeprecatedRoutes routes = new DeprecatedRoutes();

    @Test
    void listIsExactlyTheRoutesWithZeroRequests() throws IOException {
        Set<String> zeroUsage = new HashSet<>();
        for (String line : Files.readAllLines(USAGE, StandardCharsets.UTF_8).subList(1, Files.readAllLines(USAGE).size())) {
            String[] columns = line.split(",", 4);
            if (columns[1].equals("fora-do-manifesto")) {
                continue;
            }
            if (Integer.parseInt(columns[0]) == 0) {
                zeroUsage.add(columns[2] + " " + columns[3]);
            }
        }
        assertEquals(zeroUsage, routes.keys());
    }

    @Test
    void everyDeprecatedRouteExistsInTheManifest() throws IOException {
        JsonNode manifest = new ObjectMapper().readTree(Files.readString(MANIFEST, StandardCharsets.UTF_8));
        Set<String> known = new HashSet<>();
        manifest.forEach(route -> known.add(route.get("method").asText() + " " + route.get("path").asText()));
        assertTrue(known.containsAll(routes.keys()), "rota obsoleta que nao existe no manifesto");
    }

    @Test
    void routesInUseAreNeverDeprecated() {
        List<String> inUse = List.of("GET /api/v1/matrix", "GET /api/v1/employee/is-corporativo",
                "GET /api/v1/massivas/recuperar-pelo-banco", "GET /api/v1/afetados/contract/{contractId}",
                "POST /api/v1/massivas/salvar-massiva-via-api");
        for (String route : inUse) {
            assertFalse(routes.keys().contains(route), route);
        }
        assertTrue(routes.find("GET", "/api/v1/matrix").isEmpty());
    }

    @Test
    void successorIsTheUsedAliasWithTheCallVariablesFilledIn() {
        DeprecatedRoutes.Match match = routes.find("GET", "/api/v1/usuario-afetado/contract/123").orElseThrow();
        assertEquals("/api/v1/afetados/contract/123", match.route().successorFor(match.uriVariables()).orElseThrow());
    }

    @Test
    void routeWithoutAnyUsedAliasHasNoSuccessor() {
        DeprecatedRoutes.Match match = routes.find("GET", "/api/v1/estoque/movimentacao").orElseThrow();
        assertTrue(match.route().successorFor(match.uriVariables()).isEmpty());
    }

    @Test
    void methodMustMatchToo() {
        assertTrue(routes.find("POST", "/api/v1/estoque/movimentacao").isEmpty());
    }
}
