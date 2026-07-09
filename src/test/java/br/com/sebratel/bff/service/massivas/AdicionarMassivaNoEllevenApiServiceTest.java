package br.com.sebratel.bff.service.massivas;

import br.com.sebratel.bff.dto.massivas.api.AberturaRegistroMassivoAssignmentDTO;
import br.com.sebratel.bff.dto.massivas.api.AberturaRegistroMassivoInputDTO;
import br.com.sebratel.bff.dto.massivas.api.AberturaRegistroMassivoOutputDTO;
import br.com.sebratel.bff.dto.splitters.RecuperarTokenEllevenOutputDTO;
import br.com.sebratel.bff.model.Employee;
import br.com.sebratel.bff.service.EmployeeService;
import br.com.sebratel.bff.service.RecuperarTokenDoUsuarioIntegradorEllevenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdicionarMassivaNoEllevenApiServiceTest {

    @Mock
    private RecuperarTokenDoUsuarioIntegradorEllevenService recuperarTokenService;

    @Mock
    private WebClient webClient;

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private AdicionarMassivaNoEllevenApiService service;

    private SecurityContext securityContext;
    private Authentication authentication;
    private Jwt jwt;

    @BeforeEach
    void setUp() {
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
        jwt = mock(Jwt.class);

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getClaim("email")).thenReturn("test@sebratel.com.br");
        when(jwt.getClaim("name")).thenReturn("Test User");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void executarComSucesso() {
        // Arrange
        AberturaRegistroMassivoInputDTO input = new AberturaRegistroMassivoInputDTO();
        AberturaRegistroMassivoAssignmentDTO assignment = new AberturaRegistroMassivoAssignmentDTO();
        assignment.setTitle("Test Title");
        assignment.setDescription("Test Description");
        input.setAssignment(assignment);
        input.setAffectedUsers(new ArrayList<>());
        input.setAffectedUsersQuantity(5);

        RecuperarTokenEllevenOutputDTO tokenOutput = new RecuperarTokenEllevenOutputDTO("fake-token", 3600, "Bearer", "all");
        when(recuperarTokenService.executar()).thenReturn(tokenOutput);

        when(employeeService.hasB2BinInput(any())).thenReturn(false);

        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(eq(HttpHeaders.AUTHORIZATION), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        AberturaRegistroMassivoOutputDTO expectedOutput = new AberturaRegistroMassivoOutputDTO();
        when(responseSpec.bodyToMono(AberturaRegistroMassivoOutputDTO.class)).thenReturn(Mono.just(expectedOutput));

        // Act
        AberturaRegistroMassivoOutputDTO result = service.executar(input);

        // Assert
        assertEquals(expectedOutput, result);
        assertEquals(AdicionarMassivaNoEllevenApiService.NORMAL_EVENT_INCIDENT_TYPE_ID, input.getIncidentTypeId());
        verify(webClient).post();
    }

    @Test
    void executarComoEventoMassivoPelaQuantidade() {
        // Arrange
        AberturaRegistroMassivoInputDTO input = new AberturaRegistroMassivoInputDTO();
        AberturaRegistroMassivoAssignmentDTO assignment = new AberturaRegistroMassivoAssignmentDTO();
        assignment.setTitle("Test Title");
        assignment.setDescription("Test Description");
        input.setAssignment(assignment);
        input.setAffectedUsers(new ArrayList<>());
        input.setAffectedUsersQuantity(16);

        RecuperarTokenEllevenOutputDTO tokenOutput = new RecuperarTokenEllevenOutputDTO("fake-token", 3600, "Bearer", "all");
        when(recuperarTokenService.executar()).thenReturn(tokenOutput);

        when(employeeService.hasB2BinInput(any())).thenReturn(false);

        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(eq(HttpHeaders.AUTHORIZATION), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        AberturaRegistroMassivoOutputDTO expectedOutput = new AberturaRegistroMassivoOutputDTO();
        when(responseSpec.bodyToMono(AberturaRegistroMassivoOutputDTO.class)).thenReturn(Mono.just(expectedOutput));

        // Act
        AberturaRegistroMassivoOutputDTO result = service.executar(input);

        // Assert
        assertEquals(expectedOutput, result);
        assertEquals(AdicionarMassivaNoEllevenApiService.MASSIVE_EVENT_INCIDENT_TYPE_ID, input.getIncidentTypeId());
    }

    @Test
    void executarComoEventoMassivoPorTerB2B() {
        // Arrange
        AberturaRegistroMassivoInputDTO input = new AberturaRegistroMassivoInputDTO();
        AberturaRegistroMassivoAssignmentDTO assignment = new AberturaRegistroMassivoAssignmentDTO();
        assignment.setTitle("Test Title");
        assignment.setDescription("Test Description");
        input.setAssignment(assignment);
        input.setAffectedUsers(new ArrayList<>());
        input.setAffectedUsersQuantity(1);

        RecuperarTokenEllevenOutputDTO tokenOutput = new RecuperarTokenEllevenOutputDTO("fake-token", 3600, "Bearer", "all");
        when(recuperarTokenService.executar()).thenReturn(tokenOutput);

        when(employeeService.hasB2BinInput(any())).thenReturn(true);

        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(eq(HttpHeaders.AUTHORIZATION), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        AberturaRegistroMassivoOutputDTO expectedOutput = new AberturaRegistroMassivoOutputDTO();
        when(responseSpec.bodyToMono(AberturaRegistroMassivoOutputDTO.class)).thenReturn(Mono.just(expectedOutput));

        // Act
        AberturaRegistroMassivoOutputDTO result = service.executar(input);

        // Assert
        assertEquals(expectedOutput, result);
        assertEquals(AdicionarMassivaNoEllevenApiService.MASSIVE_EVENT_INCIDENT_TYPE_ID, input.getIncidentTypeId());
    }

    @Test
    void executarComErro401DevePropagarExcecao() {
        // Arrange
        AberturaRegistroMassivoInputDTO input = new AberturaRegistroMassivoInputDTO();
        AberturaRegistroMassivoAssignmentDTO assignment = new AberturaRegistroMassivoAssignmentDTO();
        assignment.setTitle("Test Title");
        assignment.setDescription("Test Description");
        input.setAssignment(assignment);
        input.setAffectedUsers(new ArrayList<>());
        input.setAffectedUsersQuantity(1);

        RecuperarTokenEllevenOutputDTO tokenOutput = new RecuperarTokenEllevenOutputDTO("invalid-token", 3600, "Bearer", "all");
        when(recuperarTokenService.executar()).thenReturn(tokenOutput);
        when(employeeService.hasB2BinInput(any())).thenReturn(false);

        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        WebClientResponseException.Unauthorized unauthorizedException = mock(WebClientResponseException.Unauthorized.class);
        when(responseSpec.bodyToMono(AberturaRegistroMassivoOutputDTO.class)).thenReturn(Mono.error(unauthorizedException));

        // Act & Assert
        assertThrows(WebClientResponseException.Unauthorized.class, () -> service.executar(input));
    }

    @Test
    void executarComWebClientResponseExceptionGeral() {
        // Arrange
        AberturaRegistroMassivoInputDTO input = new AberturaRegistroMassivoInputDTO();
        AberturaRegistroMassivoAssignmentDTO assignment = new AberturaRegistroMassivoAssignmentDTO();
        assignment.setTitle("Test Title");
        assignment.setDescription("Test Description");
        input.setAssignment(assignment);
        input.setAffectedUsers(new ArrayList<>());
        input.setAffectedUsersQuantity(1);

        RecuperarTokenEllevenOutputDTO tokenOutput = new RecuperarTokenEllevenOutputDTO("token", 3600, "Bearer", "all");
        when(recuperarTokenService.executar()).thenReturn(tokenOutput);
        when(employeeService.hasB2BinInput(any())).thenReturn(false);

        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        WebClientResponseException badRequestException = mock(WebClientResponseException.class);
        when(responseSpec.bodyToMono(AberturaRegistroMassivoOutputDTO.class)).thenReturn(Mono.error(badRequestException));

        // Act & Assert
        assertThrows(WebClientResponseException.class, () -> service.executar(input));
    }

    @Test
    void executarComExceptionGeral() {
        // Arrange
        AberturaRegistroMassivoInputDTO input = new AberturaRegistroMassivoInputDTO();
        AberturaRegistroMassivoAssignmentDTO assignment = new AberturaRegistroMassivoAssignmentDTO();
        assignment.setTitle("Test Title");
        assignment.setDescription("Test Description");
        input.setAssignment(assignment);
        input.setAffectedUsers(new ArrayList<>());
        input.setAffectedUsersQuantity(1);

        when(recuperarTokenService.executar()).thenThrow(new RuntimeException("Critical error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> service.executar(input));
    }
    @Test
    void executarComAffectedUsersNullDeveTratarComoListaVazia() {
        // Arrange
        AberturaRegistroMassivoInputDTO input = new AberturaRegistroMassivoInputDTO();
        AberturaRegistroMassivoAssignmentDTO assignment = new AberturaRegistroMassivoAssignmentDTO();
        assignment.setTitle("Test Title");
        assignment.setDescription("Test Description");
        input.setAssignment(assignment);
        input.setAffectedUsers(null); // Forçando nulo
        input.setAffectedUsersQuantity(0);

        RecuperarTokenEllevenOutputDTO tokenOutput = new RecuperarTokenEllevenOutputDTO("fake-token", 3600, "Bearer", "all");
        when(recuperarTokenService.executar()).thenReturn(tokenOutput);

        when(employeeService.hasB2BinInput(any())).thenReturn(false);

        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(eq(HttpHeaders.AUTHORIZATION), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        AberturaRegistroMassivoOutputDTO expectedOutput = new AberturaRegistroMassivoOutputDTO();
        when(responseSpec.bodyToMono(AberturaRegistroMassivoOutputDTO.class)).thenReturn(Mono.just(expectedOutput));

        // Act
        AberturaRegistroMassivoOutputDTO result = service.executar(input);

        // Assert
        assertEquals(expectedOutput, result);
        assertEquals(AdicionarMassivaNoEllevenApiService.NORMAL_EVENT_INCIDENT_TYPE_ID, input.getIncidentTypeId());
        verify(employeeService).hasB2BinInput(argThat(list -> list != null && list.isEmpty()));
    }

}
