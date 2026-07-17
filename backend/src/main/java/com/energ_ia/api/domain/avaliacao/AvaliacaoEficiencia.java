package com.energ_ia.api.domain.avaliacao;

import com.energ_ia.api.domain.cliente.Cliente;
import com.energ_ia.api.domain.core.CategoriaEficiencia;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.jackson.autoconfigure.JacksonProperties;

import java.time.LocalDate;

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

    @Column(name = "dicas_melhoria")
    private JacksonProperties.Json dicasMelhoria;
}
