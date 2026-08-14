package com.energ_ia.api.domain.cliente;

import com.energ_ia.api.domain.equipamento.EquipamentoCatalogo;
import jakarta.persistence.*;

import java.math.BigDecimal;

import com.energ_ia.api.domain.cliente.Cliente;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Cliente_Equipamento")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ClienteEquipamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "id_equipamento", nullable = false)
    private EquipamentoCatalogo equipamentoCatalogo;

    @Column(nullable = false)
    private Integer quantidade = 1;

    @Column(name = "horas_uso_diario", nullable = false, columnDefinition = "DECIMAL(4,2)")
    private Double horasUsoDiario;

    @Column(name = "dias_uso_mes", nullable = false)
    private Integer diasUsoMes;

}