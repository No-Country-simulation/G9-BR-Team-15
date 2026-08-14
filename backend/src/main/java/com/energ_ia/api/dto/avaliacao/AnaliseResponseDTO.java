package com.energ_ia.api.dto.avaliacao;

import java.util.List;

public record AnaliseResponseDTO(
        Long avaliacaoId,
        // Dados do Cálculo Local (Banco de Dados)
        Double consumoPrevistoLocalKwh,
        Double custoPrevistoLocal,

        // Dados da IA
        String categoriaIA,
        Integer scoreSustentabilidade,
        List<String> dicasMelhoria,
        Boolean alertaConsumoAlto
) {}