package com.energ_ia.api.dto.cliente;

import com.energ_ia.api.domain.core.TipoImovel;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ClienteAtualizacaoDTO(
        @JsonAlias({"nome_razao_social", "nomeRazaoSocial"})
        String nomeRazaoSocial,

        String cep,
        String cidade,
        String estado,

        @JsonAlias({"tipo_imovel", "tipoImovel"})
        TipoImovel tipoImovel
) {}