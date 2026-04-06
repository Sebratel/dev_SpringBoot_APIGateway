package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.splitters.EllevenPaginatedDTO;
import br.com.sebratel.bff.dto.splitters.EllevenSplitterResponseDTO;
import br.com.sebratel.bff.dto.splitters.NetworkComponentDTO;
import br.com.sebratel.bff.dto.splitters.RecuperarTokenEllevenOutputDTO;
import br.com.sebratel.bff.exceptions.ResourceNotFoundException;
import br.com.sebratel.bff.repository.erp.projections.SplitterProjection;
import br.com.sebratel.bff.repository.erp.splitters.SplitterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListarSplittersServiceTest {

    @Mock
    private WebClient webClient;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WebClient.Builder webClientBuilder;

    @Mock
    private RecuperarTokenDoUsuarioIntegradorEllevenService tokenService;

    @Mock
    private SplitterRepository repository;

    @InjectMocks
    private ListarSplittersService service;

    @BeforeEach
    void setUp() {
        stubWebClient();
    }

    private void stubWebClient() {
        lenient().when(webClient.mutate()).thenReturn(webClientBuilder);
        lenient().when(webClientBuilder.exchangeStrategies(any(ExchangeStrategies.class))).thenReturn(webClientBuilder);
        lenient().when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        lenient().when(webClientBuilder.build()).thenReturn(webClient);
    }

    @Test
    void executar_ShouldReturnSplitters_WhenApiSucceeds() {
        stubWebClient();
        String token = "valid-token";
        when(tokenService.executar()).thenReturn(new RecuperarTokenEllevenOutputDTO(token, 3600, "bearer", "scope"));

        EllevenSplitterResponseDTO<List<NetworkComponentDTO>> expectedResponse = new EllevenSplitterResponseDTO<>(true, null, List.of(mock(NetworkComponentDTO.class)), "type", null);

        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(expectedResponse));

        EllevenSplitterResponseDTO<List<NetworkComponentDTO>> result = service.executar();

        assertNotNull(result);
        assertEquals(1, result.response().size());
    }

    @Test
    void executarPaginado_ShouldReturnSplitters_WhenApiSucceeds() {
        stubWebClient();
        String token = "valid-token";
        when(tokenService.executar()).thenReturn(new RecuperarTokenEllevenOutputDTO(token, 3600, "bearer", "scope"));

        EllevenSplitterResponseDTO<EllevenPaginatedDTO<List<NetworkComponentDTO>>> expectedResponse = new EllevenSplitterResponseDTO<>(true, null, null, "type", null);

        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.header(anyString(), any())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(expectedResponse));

        EllevenSplitterResponseDTO<EllevenPaginatedDTO<List<NetworkComponentDTO>>> result = service.executar(0, 10);

        assertNotNull(result);
        assertTrue(result.success());
    }

    @Test
    void executarPaginado_ShouldThrowException_WhenApiFails() {
        stubWebClient();
        String token = "valid-token";
        when(tokenService.executar()).thenReturn(new RecuperarTokenEllevenOutputDTO(token, 3600, "bearer", "scope"));

        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.header(anyString(), any())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        lenient().when(responseSpec.onStatus(any(), any())).thenAnswer(invocation -> {
            Function<ClientResponse, Mono<? extends Throwable>> errorHandler = invocation.getArgument(1);
            ClientResponse response = mock(ClientResponse.class);
            lenient().when(response.statusCode()).thenReturn(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
            lenient().when(response.bodyToMono(String.class)).thenReturn(Mono.just("API Error"));
            
            Mono<? extends Throwable> errorMono = errorHandler.apply(response);
            assertNotNull(errorMono);
            return responseSpec;
        });
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.error(new RuntimeException("Falha na integração Elleven: API Error")));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.executar(0, 10));
        assertTrue(exception.getMessage().contains("Falha na integração Elleven"));
    }

    @Test
    void executar_ShouldThrowException_WhenApiFails() {
        stubWebClient();
        String token = "valid-token";
        when(tokenService.executar()).thenReturn(new RecuperarTokenEllevenOutputDTO(token, 3600, "bearer", "scope"));

        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.header(anyString(), any())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        lenient().when(responseSpec.onStatus(any(), any())).thenAnswer(invocation -> {
            Function<ClientResponse, Mono<? extends Throwable>> errorHandler = invocation.getArgument(1);
            ClientResponse response = mock(ClientResponse.class);
            lenient().when(response.statusCode()).thenReturn(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
            lenient().when(response.bodyToMono(String.class)).thenReturn(Mono.just("API Error"));
            
            Mono<? extends Throwable> errorMono = errorHandler.apply(response);
            assertNotNull(errorMono);
            return responseSpec;
        });
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.error(new RuntimeException("Falha na integração Elleven: API Error")));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.executar());
        assertTrue(exception.getMessage().contains("Falha na integração Elleven"));
    }
    @Test
    void executarPorId_ShouldReturnSplitter_WhenFound() {
        SplitterProjection projection = mock(SplitterProjection.class);
        when(repository.getSplitterById(1L)).thenReturn(Optional.of(projection));

        EllevenSplitterResponseDTO<List<NetworkComponentDTO>> result = service.executar(1L);

        assertNotNull(result);
        assertTrue(result.success());
        assertEquals(1, result.response().size());
        assertEquals(1L, result.response().get(0).id());
    }

    @Test
    void executarPorId_ShouldThrowException_WhenNotFound() {
        when(repository.getSplitterById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.executar(1L));
    }
}
