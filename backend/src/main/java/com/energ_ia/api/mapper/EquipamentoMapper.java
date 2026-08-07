package com.energ_ia.api.mapper;

import com.energ_ia.api.domain.equipamento.EquipamentoCatalogo;
import com.energ_ia.api.dto.equipamento.EquipamentoResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class EquipamentoMapper {

    public EquipamentoResponseDTO toResponseDTO(EquipamentoCatalogo equipamento) {
        if (equipamento == null) {
            return null;
        }

        return new EquipamentoResponseDTO(
                equipamento.getId(),
                equipamento.getTipo(),
                equipamento.getMarca(),
                equipamento.getModelo(),
                equipamento.getPotenciaWatts()
        );
    }
}
