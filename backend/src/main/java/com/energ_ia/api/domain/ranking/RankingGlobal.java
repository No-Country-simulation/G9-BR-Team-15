package com.energ_ia.api.domain.ranking;

import com.energ_ia.api.domain.cliente.Cliente;
import com.energ_ia.api.domain.core.CategoriaEficiencia;
import com.energ_ia.api.domain.core.TipoImovel;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "Ranking_Global")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RankingGlobal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false, unique = true)
    private Cliente cliente;

    @Column(name = "localidade", nullable = false)
    private String localidade;

    @Column(name = "nome_razao_social", nullable = false)
    private String nomeRazaoSocial;

    @Column(name = "tipo_imovel", nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoImovel tipoImovel;

    @Column(name = "tipo_ranking", nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoRanking tipoRanking;

    @Column(name = "posicao", nullable = false)
    private Integer posicao;

    @Column(name = "score_sustentabilidade", nullable = false)
    private Integer pontuacao;

    @Column(name = "categoria_eficiencia",  nullable = false)
    @Enumerated(EnumType.STRING)
    private CategoriaEficiencia categoriaEficiencia;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }
}
