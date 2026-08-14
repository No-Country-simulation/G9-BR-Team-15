package com.energ_ia.api.dto.usuario;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RegisterRequestDTO(
    @JsonProperty("nome") String nome,
    @JsonProperty("email") String email,
    @JsonProperty("senha") String senha
) {}
