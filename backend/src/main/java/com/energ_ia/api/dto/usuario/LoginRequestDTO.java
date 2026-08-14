package com.energ_ia.api.dto.usuario;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginRequestDTO(
    @JsonProperty("email") String email,
    @JsonProperty("senha") String senha
) {}
