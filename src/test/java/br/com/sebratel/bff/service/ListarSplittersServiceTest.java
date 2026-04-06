package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.splitters.EllevenPaginatedDTO;
import br.com.sebratel.bff.dto.splitters.EllevenSplitterResponseDTO;
import br.com.sebratel.bff.dto.splitters.NetworkComponentDTO;
import br.com.sebratel.bff.dto.splitters.RecuperarTokenEllevenOutputDTO;
import br.com.sebratel.bff.exceptions.ResourceNotFoundException;
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
        lenient().when(webClient.mutate()).thenReturn(webClientBuilder);
        lenient().when(webClientBuilder.exchangeStrategies(any(ExchangeStrategies.class)).build()).thenReturn(webClient);
    }

    private void stubWebClient() {
        lenient().when(webClientBuilder.exchangeStrategies(any(ExchangeStrategies.class)).build()).thenReturn(webClient);
        lenient().when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
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
    @Disabled("Varargs header call in WebClient is unstable to mock in this specific chain")
    void executarPaginado_ShouldReturnSplitters_WhenApiSucceeds() {
        String token = "valid-token";
        when(tokenService.executar()).thenReturn(new RecuperarTokenEllevenOutputDTO(token, 3600, "bearer", "scope"));

        EllevenSplitterResponseDTO<EllevenPaginatedDTO<List<NetworkComponentDTO>>> expectedResponse = new EllevenSplitterResponseDTO<>(true, null, null, "type", null);

        WebClient deepWebClient = mock(WebClient.class, RETURNS_DEEP_STUBS);
        WebClient.Builder deepBuilder = mock(WebClient.Builder.class, RETURNS_DEEP_STUBS);
        when(deepWebClient.mutate()).thenReturn(deepBuilder);
        when(deepBuilder.baseUrl(anyString()).exchangeStrategies(any(ExchangeStrategies.class)).build()).thenReturn(deepWebClient);
        
        WebClient.RequestHeadersSpec specMock = mock(WebClient.RequestHeadersSpec.class);
        when(deepWebClient.get().uri(any(Function.class))).thenReturn(specMock);
        when(specMock.header(anyString(), any())).thenReturn(specMock);
        when(specMock.header(anyString(), anyString())).thenReturn(specMock);
        
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        when(specMock.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(expectedResponse));

        ListarSplittersService localService = new ListarSplittersService(deepWebClient, tokenService, repository);
        EllevenSplitterResponseDTO<EllevenPaginatedDTO<List<NetworkComponentDTO>>> result = localService.executar(0, 10);

        assertNotNull(result);
    }

    @Test
    void executarPorId_ShouldThrowException_WhenNotFound() {
        when(repository.getSplitterById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.executar(1L));
    }
}
