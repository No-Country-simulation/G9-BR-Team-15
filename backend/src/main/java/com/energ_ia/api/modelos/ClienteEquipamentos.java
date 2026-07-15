package com.energ_ia.api.modelos;

import jakarta.persistence.*;

import java.math.BigDecimal;

import com.energ_ia.api.modelos.Cliente;

@Entity
@Table(name = "Cliente_Equipamento")
public class ClienteEquipamentos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "id_equipamento", nullable = false)
    private EquipamentoCatalogo equipamentoCatalogo;

    @Column(nullable = false)
    private Integer quantidade = 1;

    @Column(name = "horas_uso_diario", nullable = false, precision = 4, scale = 2)
    private BigDecimal horasUsoDiario;

    @Column(name = "dias_uso_mes", nullable = false)
    private Integer diasUsoMes;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public EquipamentoCatalogo getEquipamentoCatalogo() { return equipamentoCatalogo; }
    public void setEquipamentoCatalogo(EquipamentoCatalogo equipamentoCatalogo) { this.equipamentoCatalogo = equipamentoCatalogo; }
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
    public BigDecimal getHorasUsoDiario() { return horasUsoDiario; }
    public void setHorasUsoDiario(BigDecimal horasUsoDiario) { this.horasUsoDiario = horasUsoDiario; }
    public Integer getDiasUsoMes() { return diasUsoMes; }
    public void setDiasUsoMes(Integer diasUsoMes) { this.diasUsoMes = diasUsoMes; }
}