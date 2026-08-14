package com.energ_ia.api.dto.cliente;

import com.energ_ia.api.domain.core.TipoImovel;
import com.energ_ia.api.domain.core.TipoPessoa;
import java.util.List;

public record ClienteRequestDTO(
        String nomeRazaoSocial,
        TipoPessoa tipoPessoa,
        TipoImovel tipoImovel,
        String cep,
        String cidade,
        String estado,
        List<ClienteEquipamentoRequestDTO> equipamentos
) {}