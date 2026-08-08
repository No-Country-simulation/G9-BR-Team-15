package com.energ_ia.api.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record MLApiRequestDTO(
        @JsonProperty("tipo_pessoa") String tipoPessoa,
        @JsonProperty("tipo_imovel") String tipoImovel,
        List<MLEquipamentoDTO> equipamentos
) {
    public record MLEquipamentoDTO(
            String tipo,
            Integer quantidade,
            @JsonProperty("horas_uso_diario") Double horasUsoDiario,
            @JsonProperty("dias_uso_mes") Integer diasUsoMes
    ) {}
}