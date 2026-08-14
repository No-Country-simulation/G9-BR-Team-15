package com.energ_ia.api.dto.cliente;

public record ClienteEquipamentoResponseDTO(
        Long id,
        Long equipamentoId,
        String tipo,
        String marca,
        String modelo,
        Integer potenciaWatts,
        Integer quantidade,
        Double horasUsoDiario,
        Integer diasUsoMes
) {}