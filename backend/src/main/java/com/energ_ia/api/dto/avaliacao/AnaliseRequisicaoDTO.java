package com.energ_ia.api.dto.avaliacao;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AnaliseRequisicaoDTO {

    @JsonProperty("consumo_kwh")
    private Integer consumoKwh;

    @JsonProperty("uso_horario_pico")
    private Boolean usoHorarioPico;

    @JsonProperty("quantidade_equipamentos")
    private Integer quantidadeEquipamentos;

    @JsonProperty("tipo_imovel")
    private String tipoImovel;

    @JsonProperty("horas_alto_consumo")
    private Integer horasAltoConsumo;

    // Getters e Setters
    public Integer getConsumoKwh() { return consumoKwh; }
    public void setConsumoKwh(Integer consumoKwh) { this.consumoKwh = consumoKwh; }
    public Boolean getUsoHorarioPico() { return usoHorarioPico; }
    public void setUsoHorarioPico(Boolean usoHorarioPico) { this.usoHorarioPico = usoHorarioPico; }
    public Integer getQuantidadeEquipamentos() { return quantidadeEquipamentos; }
    public void setQuantidadeEquipamentos(Integer quantidadeEquipamentos) { this.quantidadeEquipamentos = quantidadeEquipamentos; }
    public String getTipoImovel() { return tipoImovel; }
    public void setTipoImovel(String tipoImovel) { this.tipoImovel = tipoImovel; }
    public Integer getHorasAltoConsumo() { return horasAltoConsumo; }
    public void setHorasAltoConsumo(Integer horasAltoConsumo) { this.horasAltoConsumo = horasAltoConsumo; }
}
