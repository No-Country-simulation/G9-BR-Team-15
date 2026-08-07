package com.energ_ia.api.controller.cliente;

import com.energ_ia.api.service.cliente.ClienteService;
import com.energ_ia.api.dto.cliente.ClienteRequestDTO;
import com.energ_ia.api.dto.cliente.ClienteResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

import com.energ_ia.api.domain.usuario.Usuario;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService service;

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> cadastrar(
            @RequestBody ClienteRequestDTO dto,
            @AuthenticationPrincipal Usuario usuarioLogado) {

        ClienteResponseDTO response = service.cadastrar(dto, usuarioLogado);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(uri).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> listar() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }
}