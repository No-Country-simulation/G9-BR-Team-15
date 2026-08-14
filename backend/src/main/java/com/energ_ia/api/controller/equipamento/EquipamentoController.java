package com.energ_ia.api.controller.equipamento;

import com.energ_ia.api.domain.equipamento.EquipamentoCatalogo;
import com.energ_ia.api.dto.equipamento.EquipamentoCatalogoAtualizacaoDTO;
import com.energ_ia.api.dto.equipamento.EquipamentoRequestDTO;
import com.energ_ia.api.dto.equipamento.EquipamentoResponseDTO;
import com.energ_ia.api.service.equipamento.EquipamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/equipamentos")
@RequiredArgsConstructor
public class EquipamentoController {

    private final EquipamentoService service;

    @PostMapping
    public ResponseEntity<EquipamentoResponseDTO> cadastrar(@RequestBody EquipamentoRequestDTO dto) {
        EquipamentoResponseDTO response = service.cadastrar(dto);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(uri).body(response);
    }

    @GetMapping
    public ResponseEntity<List<EquipamentoResponseDTO>> listar() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipamentoResponseDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquipamentoCatalogo> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid EquipamentoCatalogoAtualizacaoDTO dados) {
        EquipamentoCatalogo atualizado = service.atualizar(id, dados);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}