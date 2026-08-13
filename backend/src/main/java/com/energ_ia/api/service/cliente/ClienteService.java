package com.energ_ia.api.service.cliente;

import com.energ_ia.api.domain.cliente.Cliente;
import com.energ_ia.api.domain.cliente.ClienteEquipamento;
import com.energ_ia.api.dto.cliente.ClienteAtualizacaoDTO;
import com.energ_ia.api.dto.cliente.ClienteEquipamentoRequestDTO;
import com.energ_ia.api.infra.repository.cliente.ClienteRepository;
import com.energ_ia.api.domain.equipamento.EquipamentoCatalogo;
import com.energ_ia.api.infra.repository.equipamento.EquipamentoRepository;
import com.energ_ia.api.domain.usuario.Usuario;
import com.energ_ia.api.dto.cliente.ClienteRequestDTO;
import com.energ_ia.api.dto.cliente.ClienteResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.energ_ia.api.mapper.ClienteMapper;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final EquipamentoRepository equipamentoRepository;
    private final ClienteMapper clienteMapper;

    @Transactional
    public ClienteResponseDTO cadastrar(ClienteRequestDTO dto, Usuario usuarioLogado) {

        if (clienteRepository.existsByUsuarioAndNomeRazaoSocial(usuarioLogado, dto.nomeRazaoSocial())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Você já possui um cliente cadastrado com este Nome/Razão Social!"
            );
        }

        Cliente cliente = new Cliente();
        cliente.setNomeRazaoSocial(dto.nomeRazaoSocial());
        cliente.setTipoPessoa(dto.tipoPessoa());
        cliente.setTipoImovel(dto.tipoImovel());
        cliente.setCep(dto.cep());
        cliente.setCidade(dto.cidade());
        cliente.setEstado(dto.estado());
        cliente.setUsuario(usuarioLogado);

        cliente.setEquipamentos(new ArrayList<>());

        if (dto.equipamentos() != null && !dto.equipamentos().isEmpty()) {
            for (ClienteEquipamentoRequestDTO eqDto : dto.equipamentos()) {

                EquipamentoCatalogo equipamentoCatalogo = equipamentoRepository.findById(eqDto.equipamentoId())
                        .orElseThrow(() -> new EntityNotFoundException("Equipamento não encontrado com ID: " + eqDto.equipamentoId()));

                ClienteEquipamento relacionamento = new ClienteEquipamento();
                relacionamento.setCliente(cliente);
                relacionamento.setEquipamentoCatalogo(equipamentoCatalogo);

                relacionamento.setQuantidade(eqDto.quantidade());
                relacionamento.setHorasUsoDiario(eqDto.horasUsoDiario());
                relacionamento.setDiasUsoMes(eqDto.diasUsoMes());

                cliente.getEquipamentos().add(relacionamento);
            }
        }

        Cliente clienteSalvo = clienteRepository.save(cliente);

        return clienteMapper.toResponseDTO(clienteSalvo);
    }

    public List<ClienteResponseDTO> listarPorUsuario(Usuario usuarioLogado) {
        return clienteRepository.findByUsuario(usuarioLogado).stream()
                .map(clienteMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public ClienteResponseDTO buscarPorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado com ID: " + id));

        return clienteMapper.toResponseDTO(cliente);
    }

    @Transactional
    public ClienteResponseDTO atualizar(Long id, ClienteAtualizacaoDTO dados) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado!"));

        if (dados.nomeRazaoSocial() != null) cliente.setNomeRazaoSocial(dados.nomeRazaoSocial());
        if (dados.cep() != null) cliente.setCep(dados.cep());
        if (dados.cidade() != null) cliente.setCidade(dados.cidade());
        if (dados.estado() != null) cliente.setEstado(dados.estado());
        if (dados.tipoImovel() != null) cliente.setTipoImovel(dados.tipoImovel());

        clienteRepository.save(cliente);
        return clienteMapper.toResponseDTO(cliente);
    }

    @Transactional
    public void desativar(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado!"));
        cliente.setAtivo(false);
        cliente.setDesativado_em(LocalDateTime.now());
        clienteRepository.save(cliente);
    }

    @Transactional
    public void excluir(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado!"));

        clienteRepository.delete(cliente);
    }
}