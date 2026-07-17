package com.energ_ia.api.domain.consumo;

import com.energ_ia.api.domain.cliente.Cliente;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "Consumo_Mensal")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ConsumoMensal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @Column(name = "consumo_previsto_kwh")
    private Double consumoPrevistoKwh;

    @Column(name = "consumo_registrado_kwh")
    private Double consumoRegistradoKwh;

    @Column(name = "mes_referencia", nullable = false, unique = true)
    private LocalDate mesReferencia;

}
