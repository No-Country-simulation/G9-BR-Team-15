package com.energ_ia.api.dto;

public class AnaliseRequisicao {
    private Integer consumoKwh;
    private Boolean usoHorarioPico;
    private Integer quantidadeEquipamentos;
    private String tipoImovel;
    private Integer horasAltoConsumo;

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
