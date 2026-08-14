package com.energ_ia.api.controller.avaliacao;

import com.energ_ia.api.dto.avaliacao.TesteAnaliseRequestDTO;
import com.energ_ia.api.dto.avaliacao.TesteAnaliseResponseDTO;
import com.energ_ia.api.service.avaliacao.TesteAnaliseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/teste")
@RequiredArgsConstructor
public class TesteAnaliseController {

    private final TesteAnaliseService analiseService;

    @PostMapping("/analise-energetica")
    public ResponseEntity<TesteAnaliseResponseDTO> analisar(@RequestBody @Valid TesteAnaliseRequestDTO request) {

        TesteAnaliseResponseDTO response = analiseService.analisarConsumo(request);

        return ResponseEntity.ok(response);
    }
}