package com.energ_ia.api.dto.avaliacao;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TesteAnaliseRequisicaoDTO(
    @JsonProperty("consumo_kwh")
    @JsonAlias({"consumoKwh"})
    Integer consumoKwh,

    @JsonProperty("uso_horario_pico")
    @JsonAlias({"usoHorarioPico"})
    Boolean usoHorarioPico,

    @JsonProperty("quantidade_equipamentos")
    @JsonAlias({"quantidadeEquipamentos"})
    Integer quantidadeEquipamentos,

    @JsonProperty("tipo_imovel")
    @JsonAlias({"tipoImovel"})
    String tipoImovel,

    @JsonProperty("horas_alto_consumo")
    @JsonAlias({"horasAltoConsumo"})
    Integer horasAltoConsumo
) {}
