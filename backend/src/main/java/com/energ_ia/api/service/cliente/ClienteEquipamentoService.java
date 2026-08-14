package com.energ_ia.api.service.cliente;

import com.energ_ia.api.domain.cliente.Cliente;
import com.energ_ia.api.domain.cliente.ClienteEquipamento;
import com.energ_ia.api.domain.equipamento.EquipamentoCatalogo;
import com.energ_ia.api.dto.cliente.ClienteEquipamentoRequestDTO;
import com.energ_ia.api.dto.cliente.ClienteEquipamentoResponseDTO;
import com.energ_ia.api.dto.cliente.ClienteEquipamentoAtualizacaoDTO;
import com.energ_ia.api.infra.repository.cliente.ClienteEquipamentoRepository;
import com.energ_ia.api.infra.repository.cliente.ClienteRepository;
import com.energ_ia.api.infra.repository.equipamento.EquipamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteEquipamentoService {

    private final ClienteRepository clienteRepository;
    private final EquipamentoRepository catalogoRepository;
    private final ClienteEquipamentoRepository clienteEquipamentoRepository;

    @Transactional
    public ClienteEquipamentoResponseDTO adicionarEquipamento(Long clienteId, ClienteEquipamentoRequestDTO request) {

        Cliente cliente = buscarClienteOuLancarErro(clienteId);

        EquipamentoCatalogo catalogo = catalogoRepository.findById(request.equipamentoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipamento não encontrado no catálogo!"));

        ClienteEquipamento novoEquipamento = new ClienteEquipamento();
        novoEquipamento.setCliente(cliente);
        novoEquipamento.setEquipamentoCatalogo(catalogo);
        novoEquipamento.setQuantidade(request.quantidade());

        novoEquipamento.setHorasUsoDiario(request.horasUsoDiario());
        novoEquipamento.setDiasUsoMes(request.diasUsoMes());

        clienteEquipamentoRepository.save(novoEquipamento);

        return mapearParaResponseDTO(novoEquipamento);
    }

    public List<ClienteEquipamentoResponseDTO> listarPorCliente(Long clienteId) {

        Cliente cliente = buscarClienteOuLancarErro(clienteId);

        return cliente.getEquipamentos().stream()
                .map(this::mapearParaResponseDTO)
                .toList();
    }

    @Transactional
    public void removerEquipamento(Long clienteId, Long clienteEquipamentoId) {

        buscarClienteOuLancarErro(clienteId);

        ClienteEquipamento equipamento = clienteEquipamentoRepository.findById(clienteEquipamentoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro de equipamento não encontrado!"));

        // Validação de segurança
        if (!equipamento.getCliente().getId().equals(clienteId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para excluir este equipamento.");
        }

        clienteEquipamentoRepository.delete(equipamento);
    }

    private Cliente buscarClienteOuLancarErro(Long clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado!"));
    }

    private ClienteEquipamentoResponseDTO mapearParaResponseDTO(ClienteEquipamento entity) {
        EquipamentoCatalogo catalogo = entity.getEquipamentoCatalogo();

        return new ClienteEquipamentoResponseDTO(
                entity.getId(),
                catalogo.getId(),
                catalogo.getTipo(),
                catalogo.getMarca(),
                catalogo.getModelo(),
                catalogo.getPotenciaWatts(),
                entity.getQuantidade(),
                entity.getHorasUsoDiario(),
                entity.getDiasUsoMes()
        );
    }

    @Transactional
    public void atualizarEquipamentoCliente(Long clienteEquipamentoId, ClienteEquipamentoAtualizacaoDTO dados) {
        ClienteEquipamento ce = clienteEquipamentoRepository.findById(clienteEquipamentoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Associação de equipamento não encontrada!"));

        if (dados.quantidade() != null) {
            ce.setQuantidade(dados.quantidade());
        }

        if (dados.horasUsoDiario() != null) {
            ce.setHorasUsoDiario(dados.horasUsoDiario());
        }

        if (dados.diasUsoMes() != null) {
            ce.setDiasUsoMes(dados.diasUsoMes());
        }

        clienteEquipamentoRepository.save(ce);
    }

    @Transactional
    public void removerEquipamentoDoCliente(Long clienteEquipamentoId) {
        if (!clienteEquipamentoRepository.existsById(clienteEquipamentoId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipamento não vinculado ao cliente!");
        }
        clienteEquipamentoRepository.deleteById(clienteEquipamentoId);
    }
}