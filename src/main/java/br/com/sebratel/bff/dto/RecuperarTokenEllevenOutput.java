package br.com.sebratel.bff.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RecuperarTokenEllevenOutput(
        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("expires_in")
        Integer expiresIn,

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("scope")
        String scope
) {}