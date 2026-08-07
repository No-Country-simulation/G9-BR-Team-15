package com.energ_ia.api.service.equipamento;

import com.energ_ia.api.domain.equipamento.EquipamentoCatalogo;
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
}