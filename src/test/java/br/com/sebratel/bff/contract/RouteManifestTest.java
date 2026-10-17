package br.com.sebratel.bff.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ValueConstants;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Gera o manifesto das rotas HTTP do monolito, por reflexao nos controllers (sem subir o contexto Spring,
 * portanto sem banco). O manifesto e o contrato que o gateway precisa preservar (specs/API_GATEWAY_ENDPOINTS.md).
 *
 * <p>Sem argumentos, o teste COMPARA o manifesto gerado com o versionado e falha se divergirem: rota nova
 * ou alterada no monolito obriga a atualizar o manifesto. Para regenerar:
 * {@code ./mvnw test -Dtest=RouteManifestTest -Dmanifest.write=true}. O manifesto e o contrato que
 * o gateway (repositorio dev_SpringBoot_Gateway) precisa preservar; copie-o para la apos regenerar.
 */
class RouteManifestTest {

    private static final Path MANIFEST = Path.of("docs", "contract", "manifest.json");
    private static final int EXPECTED_ROUTES = 156;

    /** Rotas liberadas em SecurityConfig (permitAll), todas GET. */
    private static final List<String> PUBLIC_GET_PATTERNS = List.of("/api/v1/afetados/contract/**", "/api/v1/matrix");

    /** POSTs que so consultam dados: podem ser exercitados autenticados sem efeito colateral. */
    private static final Set<String> READ_ONLY_NON_GET = Set.of(
            "InventoryController#getInventory",
            "QrCodeController#recuperarQrCode",
            "SplittersController#alertasProtocoloPendente");

    /** Servico de destino de cada controller (specs/API_GATEWAY.md, secao 5). */
    private static final Map<String, String> SERVICE_BY_CONTROLLER = Map.ofEntries(
            Map.entry("AffectedUserController", "massivas"),
            Map.entry("MassiveElevenController", "massivas"),
            Map.entry("MassivaHistoryController", "massivas"),
            Map.entry("MatrixController", "massivas"),
            Map.entry("AuthenticationSitesController", "massivas"),
            Map.entry("GmudEllevenController", "massivas"),
            Map.entry("SplittersController", "splitters"),
            Map.entry("AcquisitionController", "comercial"),
            Map.entry("ActiveSellersController", "comercial"),
            Map.entry("BlockedContractController", "comercial"),
            Map.entry("ContractActivationController", "comercial"),
            Map.entry("ContractActivationInvoiceController", "comercial"),
            Map.entry("ContractPaymentController", "comercial"),
            Map.entry("ContractWithoutInvoiceController", "comercial"),
            Map.entry("DuplicateClientNameReportController", "comercial"),
            Map.entry("FirstMonthlyPayerController", "comercial"),
            Map.entry("WeeklyReportController", "comercial"),
            Map.entry("ConsumptionController", "radius"),
            Map.entry("LastConnectionController", "radius"),
            Map.entry("DuplicateCallingStationController", "radius"),
            Map.entry("DuplicatePrefixController", "radius"),
            Map.entry("FirstAuthenticationController", "radius"),
            Map.entry("InventoryController", "estoque"),
            Map.entry("InventoryMovesController", "estoque"),
            Map.entry("PendingAssetController", "estoque"),
            Map.entry("EmployeeController", "pessoas"),
            Map.entry("QrCodeController", "pessoas"));

    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Test
    void manifestMatchesControllers() throws Exception {
        List<Map<String, Object>> routes = buildRoutes();

        assertEquals(EXPECTED_ROUTES, routes.size(), "numero de rotas mudou: atualize o inventario em specs/");

        String json = mapper.writeValueAsString(routes).replace("\r\n", "\n") + "\n";
        if (Boolean.getBoolean("manifest.write")) {
            Files.createDirectories(MANIFEST.getParent());
            Files.writeString(MANIFEST, json, StandardCharsets.UTF_8);
            return;
        }
        assertTrue(Files.exists(MANIFEST), "manifesto ausente; gere com -Dmanifest.write=true");
        String committed = Files.readString(MANIFEST, StandardCharsets.UTF_8).replace("\r\n", "\n");
        assertEquals(committed, json, "rotas do monolito divergem do manifesto; regenere com -Dmanifest.write=true e revise o diff");
    }

    private List<Map<String, Object>> buildRoutes() throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));

        List<Map<String, Object>> routes = new ArrayList<>();
        for (BeanDefinition definition : scanner.findCandidateComponents("br.com.sebratel.bff.controller")) {
            Class<?> controller = Class.forName(definition.getBeanClassName());
            String service = SERVICE_BY_CONTROLLER.get(controller.getSimpleName());
            assertTrue(service != null, "controller sem servico de destino: " + controller.getSimpleName());

            RequestMapping classMapping = AnnotatedElementUtils.findMergedAnnotation(controller, RequestMapping.class);
            List<String> bases = paths(classMapping);

            for (Method method : controller.getDeclaredMethods()) {
                RequestMapping methodMapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
                if (methodMapping == null) {
                    continue;
                }
                for (String base : bases) {
                    for (String suffix : paths(methodMapping)) {
                        for (RequestMethod httpMethod : methodMapping.method()) {
                            routes.add(route(controller, method, service, httpMethod, join(base, suffix)));
                        }
                    }
                }
            }
        }
        routes.sort(Comparator.<Map<String, Object>, String>comparing(r -> (String) r.get("path"))
                .thenComparing(r -> (String) r.get("method")));
        return routes;
    }

    private Map<String, Object> route(Class<?> controller, Method method, String service, RequestMethod httpMethod, String path) {
        String handler = controller.getSimpleName() + "#" + method.getName();
        boolean isPublic = httpMethod == RequestMethod.GET && PUBLIC_GET_PATTERNS.stream()
                .anyMatch(pattern -> new AntPathMatcher().match(pattern, path));
        boolean safe = httpMethod == RequestMethod.GET || READ_ONLY_NON_GET.contains(handler);

        List<String> pathVariables = new ArrayList<>();
        List<Map<String, Object>> queryParams = new ArrayList<>();
        Set<String> headers = new LinkedHashSet<>();
        String body = null;
        for (Parameter parameter : method.getParameters()) {
            PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);
            RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
            RequestHeader requestHeader = parameter.getAnnotation(RequestHeader.class);
            RequestBody requestBody = parameter.getAnnotation(RequestBody.class);
            if (pathVariable != null) {
                pathVariables.add(named(pathVariable.name(), pathVariable.value(), parameter));
            } else if (requestParam != null) {
                Map<String, Object> query = new LinkedHashMap<>();
                query.put("name", named(requestParam.name(), requestParam.value(), parameter));
                query.put("type", parameter.getType().getSimpleName());
                boolean hasDefault = !ValueConstants.DEFAULT_NONE.equals(requestParam.defaultValue());
                query.put("required", requestParam.required() && !hasDefault);
                if (hasDefault) {
                    query.put("default", requestParam.defaultValue());
                }
                queryParams.add(query);
            } else if (requestHeader != null) {
                headers.add(named(requestHeader.name(), requestHeader.value(), parameter));
            } else if (requestBody != null) {
                body = parameter.getType().getSimpleName() + (requestBody.required() ? "" : "?");
            }
        }

        Map<String, Object> route = new LinkedHashMap<>();
        route.put("method", httpMethod.name());
        route.put("path", path);
        route.put("service", service);
        route.put("auth", isPublic ? "public" : "authenticated");
        route.put("safe", safe);
        route.put("handler", handler);
        route.put("pathVariables", pathVariables);
        route.put("queryParams", queryParams);
        route.put("headers", new ArrayList<>(headers));
        route.put("body", body);
        return route;
    }

    private static String named(String name, String value, Parameter parameter) {
        if (!name.isEmpty()) {
            return name;
        }
        return value.isEmpty() ? parameter.getName() : value;
    }

    private static List<String> paths(RequestMapping mapping) {
        if (mapping == null) {
            return List.of("");
        }
        String[] declared = mapping.path().length > 0 ? mapping.path() : mapping.value();
        return declared.length == 0 ? List.of("") : List.of(declared);
    }

    /** Mesma normalizacao do Spring: barra inicial garantida, sem barra dupla nem barra final. */
    private static String join(String base, String suffix) {
        String joined = ("/" + base + "/" + suffix).replaceAll("/+", "/");
        return joined.length() > 1 && joined.endsWith("/") ? joined.substring(0, joined.length() - 1) : joined;
    }
}
