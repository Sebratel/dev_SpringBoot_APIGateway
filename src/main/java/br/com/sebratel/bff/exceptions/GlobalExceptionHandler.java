package br.com.sebratel.bff.exceptions;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 1. Tratamento específico para o "To Be Implemented" (501)
    @ExceptionHandler(FeatureNotImplementedException.class)
    public ResponseEntity<ApiError> handleNotImplemented(FeatureNotImplementedException ex, HttpServletRequest request) {
        log.warn("Feature not implemented: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_IMPLEMENTED, ex.getMessage(), request, null);
    }

    // 2. Tratamento para Recursos Não Encontrados (404)
    @ExceptionHandler(ResourceNotFoundException.class) // Supondo que você tenha essa exception
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    // 3. Tratamento Genérico para Erros Internos (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Internal server error: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro inesperado no servidor.", request, null);
    }

    @ExceptionHandler(IntegrationEllevenException.class)
    public ResponseEntity<ApiError> handleIntegrationElleven(IntegrationEllevenException ex, HttpServletRequest request) {
        log.error("Integration Elleven error: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        log.warn("Validation errors: {}", errors);
        return buildResponse(HttpStatus.BAD_REQUEST, "Erro de validação nos dados enviados.", request, errors);
    }

    // 1. Trata quando o corpo da requisição está faltando ou é inválido (Erro 400)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        String message = "Corpo da requisição ausente ou inválido.";
        List<String> details = null;

        if (ex.getCause() instanceof InvalidFormatException ife) {
            String field = ife.getPath().stream()
                    .map(ref -> ref.getFieldName() != null ? ref.getFieldName() : "[" + ref.getIndex() + "]")
                    .collect(Collectors.joining("."));
            message = "Formato inválido para o campo: " + field;
            details = List.of("O valor '" + ife.getValue() + "' não é compatível com o tipo esperado (" + ife.getTargetType().getSimpleName() + ")");
        } else if (ex.getCause() instanceof JsonMappingException jme) {
            String field = jme.getPath().stream()
                    .map(ref -> ref.getFieldName() != null ? ref.getFieldName() : "[" + ref.getIndex() + "]")
                    .collect(Collectors.joining("."));
            message = "Erro de mapeamento no campo: " + field;
            details = List.of(jme.getOriginalMessage());
        }

        log.warn("HTTP message not readable: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, message, request, details);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = String.format("O parâmetro '%s' recebeu um valor inválido.", ex.getName());
        String detail = String.format("O valor '%s' não pôde ser convertido para o tipo %s.", 
                ex.getValue(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconhecido");
        log.warn("Type mismatch error: {}", message);
        return buildResponse(HttpStatus.BAD_REQUEST, message, request, List.of(detail));
    }

    // 2. Trata quando o método HTTP está errado (ex: GET em vez de POST) (Erro 405)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(
            org.springframework.web.HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {

        String message = String.format("O método %s não é suportado para este endpoint.", ex.getMethod());
        log.warn("HTTP method not supported: {}", message);
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, message, request, null);
    }
    
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParams(MissingServletRequestParameterException ex, HttpServletRequest request) {
        String message = String.format("O parâmetro obrigatório '%s' está ausente.", ex.getParameterName());
        log.warn("Missing servlet request parameter: {}", message);
        return buildResponse(HttpStatus.BAD_REQUEST, message, request, null);
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