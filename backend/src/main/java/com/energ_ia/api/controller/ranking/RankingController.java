package com.energ_ia.api.controller.ranking;

import com.energ_ia.api.domain.ranking.TipoRanking;
import com.energ_ia.api.dto.ranking.RankingGeralResponseDTO;
import com.energ_ia.api.dto.ranking.RankingResponseDTO;
import com.energ_ia.api.service.ranking.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rankings")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/top10")
    public ResponseEntity<List<RankingGeralResponseDTO>> listarTop10(@RequestParam TipoRanking tipo) {
        List<RankingGeralResponseDTO> top10 = rankingService.obterTop10Publico(tipo);
        return ResponseEntity.ok(top10);
    }

    @GetMapping("/clientes/{clienteId}/posicao")
    public ResponseEntity<RankingResponseDTO> buscarPosicaoCliente(
            @PathVariable Long clienteId,
            @RequestParam TipoRanking tipo) {

        RankingResponseDTO posicao = rankingService.obterPosicaoCliente(clienteId, tipo);
        return ResponseEntity.ok(posicao);
    }

    @PostMapping("/calcular")
    public ResponseEntity<Void> forcarCalculoRanking() {
        rankingService.calcularEAtualizarRanking();
        return ResponseEntity.ok().build();
    }
}