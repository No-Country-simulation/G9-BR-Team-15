package com.energ_ia.api.controller.consumo;

import com.energ_ia.api.dto.consumo.ConsumoAtualizacaoRequestDTO;
import com.energ_ia.api.dto.consumo.ConsumoRequestDTO;
import com.energ_ia.api.dto.consumo.ConsumoResponseDTO;
import com.energ_ia.api.service.consumo.ConsumoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clientes/{clienteId}/consumos")
@RequiredArgsConstructor
public class ConsumoMensalController {

    private final ConsumoService service;

    @PostMapping
    public ResponseEntity<ConsumoResponseDTO> criar(
            @PathVariable Long clienteId,
            @RequestBody @Valid ConsumoRequestDTO dados) {
        ConsumoResponseDTO response = service.criar(clienteId, dados);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ConsumoResponseDTO>> listar(@PathVariable Long clienteId) {
        List<ConsumoResponseDTO> lista = service.listarPorCliente(clienteId);
        return ResponseEntity.ok(lista);
    }

    @PutMapping("/{consumoId}")
    public ResponseEntity<ConsumoResponseDTO> atualizar(
            @PathVariable Long clienteId,
            @PathVariable Long consumoId,
            @RequestBody @Valid ConsumoAtualizacaoRequestDTO dados) {
        ConsumoResponseDTO response = service.atualizar(clienteId, consumoId, dados);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{consumoId}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long clienteId,
            @PathVariable Long consumoId) {
        service.excluir(clienteId, consumoId);
        return ResponseEntity.noContent().build();
    }
}