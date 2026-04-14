package br.com.sebratel.bff.exceptions;

import br.com.sebratel.bff.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    void shouldHandleFeatureNotImplementedException() {
        FeatureNotImplementedException ex = new FeatureNotImplementedException("Not implemented");
        ResponseEntity<ApiError> response = exceptionHandler.handleNotImplemented(ex, request);
        assertEquals(HttpStatus.NOT_IMPLEMENTED, response.getStatusCode());
        assertEquals("Not implemented", response.getBody().getMessage());
    }

    @Test
    void shouldHandleResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found");
        ResponseEntity<ApiError> response = exceptionHandler.handleNotFound(ex, request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not found", response.getBody().getMessage());
    }

    @Test
    void shouldHandleIntegrationEllevenException() {
        IntegrationEllevenException ex = new IntegrationEllevenException("Integration error");
        ResponseEntity<ApiError> response = exceptionHandler.handleIntegrationElleven(ex, request);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals("Integration error", response.getBody().getMessage());
    }

    @Test
    void shouldHandleDataIntegrityViolationException() {
        DataIntegrityViolationException ex = mock(DataIntegrityViolationException.class);
        Throwable rootCause = new Throwable("Column 'field' cannot be null");
        when(ex.getRootCause()).thenReturn(rootCause);
        
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleDataIntegrityViolation(ex, request);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody().getErrors());
    }

    @Test
    void shouldHandleGenericException() {
        Exception ex = new Exception("Generic error");
        ResponseEntity<ApiError> response = exceptionHandler.handleGenericException(ex, request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Ocorreu um erro inesperado no servidor.", response.getBody().getMessage());
    }
}
