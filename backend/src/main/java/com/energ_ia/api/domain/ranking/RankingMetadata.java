package com.energ_ia.api.domain.ranking;

import com.energ_ia.api.domain.core.StatusProcessamento;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "Ranking_Metadata")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RankingMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "nome_job", nullable = false)
    private String job;

    @Column(name = "status_job", nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusProcessamento status;

    @Column(name = "proxima_atualizacao")
    private LocalDateTime proximaAtualizacao;

    @Column(name = "ultima_atualizacao")
    private LocalDateTime ultimaAtualizacao;
}
