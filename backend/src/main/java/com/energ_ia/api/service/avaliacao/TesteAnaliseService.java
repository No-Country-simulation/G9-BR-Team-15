package com.energ_ia.api.service.avaliacao;

import com.energ_ia.api.infra.client.mlservice.MLApiCliente;
import com.energ_ia.api.dto.avaliacao.TesteAnaliseRequestDTO;
import com.energ_ia.api.dto.avaliacao.TesteAnaliseResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TesteAnaliseService {

    private final MLApiCliente mlApiCliente;

    private static final double TARIFA_KWH = 0.75;

    public TesteAnaliseResponseDTO analisarConsumo(TesteAnaliseRequestDTO request) {

        TesteAnaliseResponseDTO responseML = mlApiCliente.chamarApiObrigatoria(request);

        Double custoEstimado = request.consumoKwh() * TARIFA_KWH;

        return new TesteAnaliseResponseDTO(
                responseML.categoria(),
                responseML.probabilidade(),
                responseML.recomendacoes(),
                custoEstimado
        );
    }
}