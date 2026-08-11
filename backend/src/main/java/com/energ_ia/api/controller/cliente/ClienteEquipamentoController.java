package com.energ_ia.api.controller.cliente;

import com.energ_ia.api.dto.cliente.ClienteEquipamentoRequestDTO;
import com.energ_ia.api.dto.cliente.ClienteEquipamentoResponseDTO;
import com.energ_ia.api.dto.cliente.ClienteEquipamentoAtualizacaoDTO;
import com.energ_ia.api.service.cliente.ClienteEquipamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clientes/{clienteId}/equipamentos")
@RequiredArgsConstructor
public class ClienteEquipamentoController {

    private final ClienteEquipamentoService service;

    @GetMapping
    public ResponseEntity<List<ClienteEquipamentoResponseDTO>> listarEquipamentos(
            @PathVariable Long clienteId) {

        List<ClienteEquipamentoResponseDTO> lista = service.listarPorCliente(clienteId);
        return ResponseEntity.ok(lista);
    }

    @PostMapping
    public ResponseEntity<ClienteEquipamentoResponseDTO> adicionarEquipamento(
            @PathVariable Long clienteId,
            @RequestBody @Valid ClienteEquipamentoRequestDTO request) {

        ClienteEquipamentoResponseDTO response = service.adicionarEquipamento(clienteId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @DeleteMapping("/{equipamentoId}")
    public ResponseEntity<Void> removerEquipamento(
            @PathVariable Long clienteId,
            @PathVariable Long equipamentoId) {

        service.removerEquipamento(clienteId, equipamentoId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{clienteEquipamentoId}")
    public ResponseEntity<Void> atualizarEquipamento(@PathVariable Long clienteEquipamentoId, @RequestBody @Valid ClienteEquipamentoAtualizacaoDTO dados) {
        service.atualizarEquipamentoCliente(clienteEquipamentoId, dados);
        return ResponseEntity.noContent().build();
    }
}