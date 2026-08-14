package com.energ_ia.api.dto.avaliacao;

import com.fasterxml.jackson.annotation.JsonAlias;

public record AnaliseRequestDTO(
        @JsonAlias({"consumo_registrado_kwh", "consumoRegistradoKwh"})
        Double consumoRegistradoKwh
) {
}
