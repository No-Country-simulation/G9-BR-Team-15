package com.energ_ia.api.dto.cliente;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ClienteEquipamentoRequestDTO(
        @NotNull
        Long equipamentoId,

        @NotNull @Min(1)
        Integer quantidade,

        @NotNull
        Double horasUsoDiario,

        @NotNull
        Integer diasUsoMes
) {}