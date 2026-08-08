package com.energ_ia.api.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record MLApiResponseDTO(
        String categoria,
        Double probabilidade,
        List<String> recomendacoes,
        @JsonProperty("consumo_estimado_kwh") Double consumoEstimadoKwh,
        @JsonProperty("custo_estimado_mensal") Double custoEstimadoMensal,
        @JsonProperty("alerta_consumo_alto") Boolean alertaConsumoAlto
) {}