package com.energ_ia.api.modelos;

import jakarta.persistence.*;

@Entity
@Table(name = "Equipamento_Catalogo")
public class EquipamentoCatalogo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String tipo;

    @Column(nullable = false, length = 100)
    private String marca;

    @Column(nullable = false, length = 100)
    private String modelo;

    @Column(name = "potencia_watts", nullable = false)
    private Integer potenciaWatts;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public Integer getPotenciaWatts() { return potenciaWatts; }
    public void setPotenciaWatts(Integer potenciaWatts) { this.potenciaWatts = potenciaWatts; }
}