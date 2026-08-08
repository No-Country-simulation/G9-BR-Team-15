package com.energ_ia.api.dto.avaliacao;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record TesteAnaliseResponseDTO(
    @JsonProperty("categoria") String categoria,
    @JsonProperty("probabilidade") Double probabilidade,
    @JsonProperty("recomendacoes") List<String> recomendacoes,
    @JsonProperty("custo_estimado_mensal") Double custoEstimadoMensal
) {}
