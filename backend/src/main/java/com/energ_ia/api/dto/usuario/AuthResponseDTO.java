package com.energ_ia.api.dto.usuario;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthResponseDTO(
    @JsonProperty("id") Long id,
    @JsonProperty("nome") String nome,
    @JsonProperty("email") String email,
    @JsonProperty("mensagem") String mensagem,
    @JsonProperty("token") String token
) {}
