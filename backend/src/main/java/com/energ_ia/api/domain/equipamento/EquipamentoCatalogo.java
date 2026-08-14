package com.energ_ia.api.domain.equipamento;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Equipamento_Catalogo")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EquipamentoCatalogo {
    @Id
    @EqualsAndHashCode.Include
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

    public EquipamentoCatalogo(String tipo, String marca, String modelo, Integer integer) {
        this.tipo = tipo;
        this.marca = marca;
        this.modelo = modelo;
        this.potenciaWatts = integer;
    }

    public void setModelos(String modelo) {
        this.modelo = modelo;
    }
}