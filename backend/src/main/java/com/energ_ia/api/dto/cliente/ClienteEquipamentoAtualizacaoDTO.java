package com.energ_ia.api.dto.cliente;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ClienteEquipamentoAtualizacaoDTO(
        @Min(value = 1, message = "A quantidade deve ser no mínimo 1")
        Integer quantidade,

        @DecimalMin(value = "0.1", message = "As horas de uso diário devem ser maiores que zero")
        @JsonAlias({"horas_uso_diario", "horasUsoDiario"})
        Double horasUsoDiario,

        @Min(value = 1, message = "Os dias de uso no mês devem ser no mínimo 1")
        @JsonAlias({"dias_uso_mes", "diasUsoMes"})
        Integer diasUsoMes
) {}