package com.energ_ia.api.service.consumo;

import com.energ_ia.api.domain.cliente.Cliente;
import com.energ_ia.api.domain.consumo.ConsumoMensal;
import com.energ_ia.api.dto.consumo.ConsumoRequestDTO;
import com.energ_ia.api.dto.consumo.ConsumoResponseDTO;
import com.energ_ia.api.infra.repository.cliente.ClienteRepository;
import com.energ_ia.api.infra.repository.consumo.ConsumoMensalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsumoService {

    private final ConsumoMensalRepository repository;
    private final ClienteRepository clienteRepository;

    @Transactional
    public ConsumoResponseDTO criar(Long clienteId, ConsumoRequestDTO dados) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado!"));

        ConsumoMensal consumo = new ConsumoMensal();
        consumo.setCliente(cliente);
        consumo.setMesReferencia(dados.mesReferencia());
        consumo.setConsumoRegistradoKwh(dados.consumoRegistradoKwh());

        repository.save(consumo);
        return mapearParaDTO(consumo);
    }

    public List<ConsumoResponseDTO> listarPorCliente(Long clienteId) {
        if (!clienteRepository.existsById(clienteId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado!");
        }
        return repository.findByClienteId(clienteId).stream()
                .map(this::mapearParaDTO)
                .toList();
    }

    @Transactional
    public ConsumoResponseDTO atualizar(Long clienteId, Long consumoId, ConsumoRequestDTO dados) {
        ConsumoMensal consumo = repository.findById(consumoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro de consumo não encontrado!"));

        if (!consumo.getCliente().getId().equals(clienteId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este consumo não pertence ao cliente informado.");
        }

        if (dados.mesReferencia() != null) consumo.setMesReferencia(dados.mesReferencia());
        if (dados.consumoRegistradoKwh() != null) consumo.setConsumoRegistradoKwh(dados.consumoRegistradoKwh());

        repository.save(consumo);
        return mapearParaDTO(consumo);
    }

    @Transactional
    public void excluir(Long clienteId, Long consumoId) {
        ConsumoMensal consumo = repository.findById(consumoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro de consumo não encontrado!"));

        if (!consumo.getCliente().getId().equals(clienteId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este consumo não pertence ao cliente informado.");
        }

        repository.delete(consumo);
    }

    private ConsumoResponseDTO mapearParaDTO(ConsumoMensal c) {
        return new ConsumoResponseDTO(
                c.getId(),
                c.getCliente().getId(),
                c.getMesReferencia(),
                c.getConsumoRegistradoKwh(),
                c.getConsumoPrevistoKwh(),
                c.getConsumoEstimadoIaKwh()
        );
    }
}