package com.energ_ia.api.domain.avaliacao;

import java.time.LocalDate;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.energ_ia.api.domain.cliente.Cliente;
import com.energ_ia.api.domain.core.CategoriaEficiencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Avaliacao_Eficiencia")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AvaliacaoEficiencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @Column(name = "mes_referencia", nullable = false)
    private LocalDate mesReferencia;

    @Column(name = "score_sustentabilidade", nullable = false)
    private Integer scoreSustentabilidade;

    @Column(name = "categoria_eficiencia", nullable = false)
    @Enumerated(EnumType.STRING)
    private CategoriaEficiencia categoriaEficiencia;

    @JdbcTypeCode(SqlTypes.JSON)
    private String dicasMelhoria;  // ou Map<String, Object>
}
