package com.energ_ia.api.dto.cliente;

import java.math.BigDecimal;

public record ClienteEquipamentoResponseDTO(
        Long equipamentoId,
        String tipo,
        String marca,
        String modelo,
        Integer potenciaWatts,
        Integer quantidade,
        BigDecimal horasUsoDiario,
        Integer diasUsoMes
) {}