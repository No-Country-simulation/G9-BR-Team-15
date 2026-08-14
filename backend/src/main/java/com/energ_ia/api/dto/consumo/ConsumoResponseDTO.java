package com.energ_ia.api.dto.consumo;

import java.time.LocalDate;

public record ConsumoResponseDTO(
        Long id,
        Long clienteId,
        LocalDate mesReferencia,
        Double consumoRegistradoKwh,
        Double consumoPrevistoKwh,
        Double consumoEstimadoIaKwh
) {}