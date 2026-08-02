package com.energ_ia.api.service.equipamento;

import com.energ_ia.api.domain.equipamento.EquipamentoCatalogo;
import com.energ_ia.api.dto.equipamento.EquipamentoRequestDTO;
import com.energ_ia.api.dto.equipamento.EquipamentoResponseDTO;
import com.energ_ia.api.infra.repository.equipamento.EquipamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EquipamentoService {

    private final EquipamentoRepository repository;

    @Transactional
    public EquipamentoResponseDTO cadastrar(EquipamentoRequestDTO dto) {

        var novoEquipamento = new EquipamentoCatalogo(
                dto.tipo(),
                dto.marca(),
                dto.modelo(),
                dto.potenciaWatts()
        );

        var equipamentoSalvo = repository.save(novoEquipamento);

        return new EquipamentoResponseDTO(equipamentoSalvo);
    }

    @Transactional(readOnly = true)
    public List<EquipamentoResponseDTO> listarTodos() {
        return repository.findAll().stream()
                .map(EquipamentoResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EquipamentoResponseDTO buscarPorId(Long id) {
        var equipamento = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipamento não encontrado!"));

        return new EquipamentoResponseDTO(equipamento);
    }
}
