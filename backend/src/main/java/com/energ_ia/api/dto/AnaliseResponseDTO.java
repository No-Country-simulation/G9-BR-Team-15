package com.energ_ia.api.dto;

import java.util.List;

public class AnaliseResponseDTO {
    private String categoria;
    private Double probabilidade;
    private List<String> recomendacoes;
    private Double custoEstimadoMensal;

    // Construtores, getters e setters
    public AnaliseResponseDTO() {}

    public AnaliseResponseDTO(String categoria, Double probabilidade, List<String> recomendacoes, Double custoEstimadoMensal) {
        this.categoria = categoria;
        this.probabilidade = probabilidade;
        this.recomendacoes = recomendacoes;
        this.custoEstimadoMensal = custoEstimadoMensal;
    }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public Double getProbabilidade() { return probabilidade; }
    public void setProbabilidade(Double probabilidade) { this.probabilidade = probabilidade; }
    public List<String> getRecomendacoes() { return recomendacoes; }
    public void setRecomendacoes(List<String> recomendacoes) { this.recomendacoes = recomendacoes; }
    public Double getCustoEstimadoMensal() { return custoEstimadoMensal; }
    public void setCustoEstimadoMensal(Double custoEstimadoMensal) { this.custoEstimadoMensal = custoEstimadoMensal; }
}