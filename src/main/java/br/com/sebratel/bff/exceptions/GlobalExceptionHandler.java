package br.com.sebratel.bff.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Tratamento específico para o "To Be Implemented" (501)
    @ExceptionHandler(FeatureNotImplementedException.class)
    public ResponseEntity<ApiError> handleNotImplemented(FeatureNotImplementedException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_IMPLEMENTED, ex.getMessage(), request, null);
    }

    // 2. Tratamento para Recursos Não Encontrados (404)
    @ExceptionHandler(ResourceNotFoundException.class) // Supondo que você tenha essa exception
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    // 3. Tratamento Genérico para Erros Internos (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro inesperado no servidor.", request, null);
    }

    @ExceptionHandler(IntegrationEllevenException.class)
    public ResponseEntity<ApiError> handleIntegrationElleven(IntegrationEllevenException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        return buildResponse(HttpStatus.BAD_REQUEST, "Erro de validação nos dados enviados.", request, errors);
    }


    private ResponseEntity<ApiError> buildResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            List<String> details
    ) {
        ApiError error = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .details(details)
                .build();
        return new ResponseEntity<>(error, status);
    }
}