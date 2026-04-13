package br.com.sebratel.bff.dto;

public record DhoUserDTO(
    Integer id,
    String name,
    String email,
    String accessLevel
) {}
