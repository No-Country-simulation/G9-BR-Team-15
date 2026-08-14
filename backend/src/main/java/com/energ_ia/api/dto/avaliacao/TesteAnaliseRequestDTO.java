package com.energ_ia.api.dto.avaliacao;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TesteAnaliseRequestDTO(

        @NotNull(message = "O consumo é obrigatório")
        @Positive(message = "Consumo kWh deve ser maior que zero")
        @JsonAlias({"consumo_kwh", "consumoKwh"})
        Integer consumoKwh,

        @JsonAlias({"uso_horario_pico", "usoHorarioPico"})
        Boolean usoHorarioPico,

        @NotNull(message = "A quantidade de equipamentos é obrigatória")
        @Min(value = 0, message = "Quantidade de equipamentos não pode ser negativa")
        @JsonAlias({"quantidade_equipamentos", "quantidadeEquipamentos"})
        Integer quantidadeEquipamentos,

        @JsonAlias({"tipo_imovel", "tipoImovel"})
        String tipoImovel,

        @JsonAlias({"horas_alto_consumo", "horasAltoConsumo"})
        Integer horasAltoConsumo
) {}