package com.energ_ia.api.infra.repository.ranking;

import com.energ_ia.api.domain.ranking.RankingMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RankingMetadataRepository extends JpaRepository<RankingMetadata, Long> {

    Optional<RankingMetadata> findByJob(String nomeJob);
}