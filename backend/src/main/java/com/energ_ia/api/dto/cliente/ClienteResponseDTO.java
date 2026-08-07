package com.energ_ia.api.dto.cliente;

import com.energ_ia.api.domain.core.TipoImovel;
import com.energ_ia.api.domain.core.TipoPessoa;
import java.time.LocalDateTime;
import java.util.List;

public record ClienteResponseDTO(
        Long id,
        String nomeRazaoSocial,
        TipoPessoa tipoPessoa,
        TipoImovel tipoImovel,
        String cep,
        String cidade,
        String estado,
        Boolean ativo,
        List<ClienteEquipamentoResponseDTO> equipamentos
) { }