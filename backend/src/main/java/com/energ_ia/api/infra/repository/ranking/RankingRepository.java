package com.energ_ia.api.infra.repository.ranking;

import com.energ_ia.api.domain.ranking.RankingGlobal;
import com.energ_ia.api.domain.ranking.TipoRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RankingRepository extends JpaRepository<RankingGlobal, Long> {

    List<RankingGlobal> findTop10ByTipoRankingOrderByPosicaoAsc(TipoRanking tipoRanking);

    Optional<RankingGlobal> findByClienteIdAndTipoRanking(Long clienteId, TipoRanking tipoRanking);
}