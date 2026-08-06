package com.energ_ia.api.service;

import com.energ_ia.api.domain.cliente.MLApiCliente;
import com.energ_ia.api.dto.avaliacao.AnaliseRequisicaoDTO;
import com.energ_ia.api.dto.avaliacao.AnaliseResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnaliseService {

    @Autowired
    private MLApiCliente mlApiCliente;

    // Método para usuário NÃO logado (endpoint obrigatório)
    public AnaliseResponseDTO analisarConsumo(AnaliseRequisicaoDTO request) {
        // Validações básicas
        validarRequest(request);
        return mlApiCliente.chamarApiObrigatoria(request);
    }

    // Método para usuário LOGADO (com persistência)
    public AnaliseResponseDTO analisarESalvar(AnaliseRequisicaoDTO request, Long clienteId) {
        validarRequest(request);
        AnaliseResponseDTO response = mlApiCliente.chamarApiUsuarioLogado(request);
        // TODO: salvar no banco (AvaliacaoEficiencia)
        return response;
    }

    private void validarRequest(AnaliseRequisicaoDTO request) {
        if (request.getConsumoKwh() == null || request.getConsumoKwh() <= 0) {
            throw new IllegalArgumentException("Consumo kWh deve ser maior que zero");
        }
        if (request.getQuantidadeEquipamentos() == null || request.getQuantidadeEquipamentos() < 0) {
            throw new IllegalArgumentException("Quantidade de equipamentos não pode ser negativa");
        }
    }
}
