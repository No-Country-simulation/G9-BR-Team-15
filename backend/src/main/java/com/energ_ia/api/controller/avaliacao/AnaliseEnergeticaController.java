package com.energ_ia.api.controller.avaliacao;

import com.energ_ia.api.dto.avaliacao.AnaliseResponseDTO;
import com.energ_ia.api.service.avaliacao.AnaliseEnergeticaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class AnaliseEnergeticaController {

    private final AnaliseEnergeticaService analiseService;

    @PostMapping("/{clienteId}/analise-energetica")
    public ResponseEntity<AnaliseResponseDTO> gerarAnaliseOficial(@PathVariable Long clienteId) {

        AnaliseResponseDTO response = analiseService.gerarAnaliseCompleta(clienteId);

        return ResponseEntity.ok(response);
    }
}