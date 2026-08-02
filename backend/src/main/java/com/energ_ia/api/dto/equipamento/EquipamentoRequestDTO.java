package com.energ_ia.api.dto.equipamento;

public record EquipamentoRequestDTO(
        String tipo,
        String marca,
        String modelo,
        Integer potenciaWatts
) {
}
