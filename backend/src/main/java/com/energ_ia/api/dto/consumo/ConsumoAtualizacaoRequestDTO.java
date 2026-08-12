package com.energ_ia.api.dto.consumo;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ConsumoAtualizacaoRequestDTO(
        @JsonAlias({"mes_referencia", "mesReferencia"})
        LocalDate mesReferencia,

        @NotNull(message = "O consumo registrado é obrigatório")
        @DecimalMin(value = "0.0", inclusive = false, message = "O consumo deve ser maior que zero")
        @JsonAlias({"consumo_registrado_kwh", "consumoRegistradoKwh"})
        Double consumoRegistradoKwh
) {}