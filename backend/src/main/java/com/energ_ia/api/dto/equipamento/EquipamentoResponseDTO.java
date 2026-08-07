package com.energ_ia.api.dto.equipamento;

import com.energ_ia.api.domain.equipamento.EquipamentoCatalogo;

public record EquipamentoResponseDTO(
        Long id,
        String tipo,
        String marca,
        String modelo,
        Integer potenciaWatts
) {
}
