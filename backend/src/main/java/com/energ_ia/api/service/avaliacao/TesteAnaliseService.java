package com.energ_ia.api.service;

import com.energ_ia.api.infra.client.mlservice.MLApiCliente;
import com.energ_ia.api.dto.avaliacao.TesteAnaliseRequisicaoDTO;
import com.energ_ia.api.dto.avaliacao.TesteAnaliseResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnaliseService {

    @Autowired
    private MLApiCliente mlApiCliente;

    public TesteAnaliseResponseDTO analisarConsumo(TesteAnaliseRequisicaoDTO request) {
        validarRequest(request);
        TesteAnaliseResponseDTO response = mlApiCliente.chamarApiObrigatoria(request);
        Double custo = request.consumoKwh() * 0.75;
        return new TesteAnaliseResponseDTO(
            response.categoria(),
            response.probabilidade(),
            response.recomendacoes(),
            custo
        );
    }

    private void validarRequest(TesteAnaliseRequisicaoDTO request) {
        if (request.consumoKwh() == null || request.consumoKwh() <= 0) {
            throw new IllegalArgumentException("Consumo kWh deve ser maior que zero");
        }
        if (request.quantidadeEquipamentos() == null || request.quantidadeEquipamentos() < 0) {
            throw new IllegalArgumentException("Quantidade de equipamentos não pode ser negativa");
        }
    }
}
