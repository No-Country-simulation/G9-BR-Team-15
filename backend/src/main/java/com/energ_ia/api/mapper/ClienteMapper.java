package com.energ_ia.api.mapper;

import com.energ_ia.api.domain.cliente.Cliente;
import com.energ_ia.api.dto.cliente.ClienteEquipamentoResponseDTO;
import com.energ_ia.api.dto.cliente.ClienteResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {

    public ClienteResponseDTO toResponseDTO(Cliente cliente) {
        if (cliente == null) {
            return null;
        }

        var equipamentosDTO = cliente.getEquipamentos().stream()
                .map(eq -> new ClienteEquipamentoResponseDTO(
                        eq.getId(), 
                        eq.getEquipamentoCatalogo().getId(),
                        eq.getEquipamentoCatalogo().getTipo(),
                        eq.getEquipamentoCatalogo().getMarca(),
                        eq.getEquipamentoCatalogo().getModelo(),
                        eq.getEquipamentoCatalogo().getPotenciaWatts(),
                        eq.getQuantidade(),
                        eq.getHorasUsoDiario(),
                        eq.getDiasUsoMes()
                ))
                .toList();

        return new ClienteResponseDTO(
                cliente.getId(),
                cliente.getNomeRazaoSocial(),
                cliente.getTipoPessoa(),
                cliente.getTipoImovel(),
                cliente.getCep(),
                cliente.getCidade(),
                cliente.getEstado(),
                cliente.getAtivo(),
                equipamentosDTO
        );
    }
}