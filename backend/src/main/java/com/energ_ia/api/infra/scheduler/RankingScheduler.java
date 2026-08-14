package com.energ_ia.api.infra.scheduler;

import com.energ_ia.api.service.ranking.RankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RankingScheduler {

    private final RankingService rankingService;

    //todo dia às 23:00:00
    @Scheduled(cron = "0 0 23 * * *", zone = "America/Sao_Paulo")
    public void atualizarRankingDiario() {
        log.info("Iniciando a atualização diária do ranking...");
        try {
            rankingService.calcularEAtualizarRanking();
            log.info("Ranking atualizado com sucesso!");
        } catch (Exception e) {
            log.error("Erro ao atualizar o ranking diário: {}", e.getMessage(), e);
        }
    }
}