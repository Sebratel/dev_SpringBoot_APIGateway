package br.com.sebratel.bff.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private final boolean success;
    private final String message;
    private final T data;
    private final Object details;
    private final LocalDateTime timestamp = LocalDateTime.now();
}