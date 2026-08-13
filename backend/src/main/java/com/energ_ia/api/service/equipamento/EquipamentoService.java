package com.energ_ia.api.service.equipamento;

import com.energ_ia.api.domain.equipamento.EquipamentoCatalogo;
import com.energ_ia.api.dto.equipamento.EquipamentoCatalogoAtualizacaoDTO;
import com.energ_ia.api.dto.equipamento.EquipamentoRequestDTO;
import com.energ_ia.api.dto.equipamento.EquipamentoResponseDTO;
import com.energ_ia.api.infra.repository.equipamento.EquipamentoRepository;
import com.energ_ia.api.mapper.EquipamentoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EquipamentoService {

    private final EquipamentoRepository repository;

    private final EquipamentoMapper mapper;

    @Transactional
    public EquipamentoResponseDTO cadastrar(EquipamentoRequestDTO dto) {
        if (repository.existsByTipoAndMarcaAndModelo(dto.tipo(), dto.marca(), dto.modelo())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Um equipamento dessa marca e modelo já está cadastrado no catálogo!"
            );
        }

        var novoEquipamento = new EquipamentoCatalogo(
                dto.tipo(),
                dto.marca(),
                dto.modelo(),
                dto.potenciaWatts()
        );

        var equipamentoSalvo = repository.save(novoEquipamento);

        return mapper.toResponseDTO(equipamentoSalvo);
    }

    @Transactional(readOnly = true)
    public List<EquipamentoResponseDTO> listarTodos() {
        return repository.findAll().stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EquipamentoResponseDTO buscarPorId(Long id) {
        var equipamento = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipamento não encontrado!"));

        return mapper.toResponseDTO(equipamento);
    }

    @Transactional
    public EquipamentoCatalogo atualizar(Long id, EquipamentoCatalogoAtualizacaoDTO dados) {
        EquipamentoCatalogo item = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipamento do catálogo não encontrado!"));

        if (dados.tipo() != null) item.setTipo(dados.tipo());
        if (dados.marca() != null) item.setMarca(dados.marca());
        if (dados.modelo() != null) item.setModelos(dados.modelo());
        if (dados.potenciaWatts() != null) item.setPotenciaWatts(dados.potenciaWatts());

        return repository.save(item);
    }

    @Transactional
    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipamento do catálogo não encontrado!");
        }

        try {
            repository.deleteById(id);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Não é possível excluir este equipamento do catálogo pois ele está vinculado a um ou mais clientes.");
        }
    }
}