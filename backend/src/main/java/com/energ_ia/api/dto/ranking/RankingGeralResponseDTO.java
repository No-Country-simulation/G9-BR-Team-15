package com.energ_ia.api.dto.ranking;

import com.energ_ia.api.domain.core.CategoriaEficiencia;
import com.energ_ia.api.domain.core.TipoImovel;
import com.energ_ia.api.domain.ranking.TipoRanking;

import java.time.LocalDateTime;

public record RankingGeralResponseDTO(
        Integer posicao,
        String nomeRazaoSocial,
        String localidade,
        TipoImovel tipoImovel,
        Integer pontuacao,
        CategoriaEficiencia categoriaEficiencia,
        LocalDateTime atualizadoEm
) {
}
