package com.energ_ia.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

/**
 * DTO para receber os dados de consumo de um cliente.
 * Usado no endpoint POST /api/clientes/{clienteId}/consumo.
 */
public class ConsumoRequestDTO {

    @NotNull(message = "Mês de referência é obrigatório")
    @JsonProperty("mes_referencia")
    private LocalDate mesReferencia;

    @NotNull(message = "Consumo registrado é obrigatório")
    @Positive(message = "Consumo deve ser maior que zero")
    @JsonProperty("consumo_registrado_kwh")
    private Double consumoRegistradoKwh;


    // getters e setters
    public LocalDate getMesReferencia() { return mesReferencia; }
    public void setMesReferencia(LocalDate mesReferencia) { this.mesReferencia = mesReferencia; }

    public Double getConsumoRegistradoKwh() { return consumoRegistradoKwh; }
    public void setConsumoRegistradoKwh(Double consumoRegistradoKwh) { this.consumoRegistradoKwh = consumoRegistradoKwh; }

    }