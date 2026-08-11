package com.energ_ia.api.dto.equipamento;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EquipamentoCatalogoAtualizacaoDTO(
        String tipo,
        String marca,
        String modelo,

        @JsonAlias({"potencia_watts", "potenciaWatts"})
        Integer potenciaWatts
) {}