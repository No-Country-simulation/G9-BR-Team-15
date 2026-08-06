package com.energ_ia.api.dto.avaliacao;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AnaliseRequisicaoDTO(
    @JsonProperty("consumo_kwh") Integer consumoKwh,
    @JsonProperty("uso_horario_pico") Boolean usoHorarioPico,
    @JsonProperty("quantidade_equipamentos") Integer quantidadeEquipamentos,
    @JsonProperty("tipo_imovel") String tipoImovel,
    @JsonProperty("horas_alto_consumo") Integer horasAltoConsumo
) {}
