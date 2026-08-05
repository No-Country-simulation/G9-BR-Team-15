package com.energ_ia.api.controller.avaliacao;

import com.energ_ia.api.dto.avaliacao.AnaliseRequisicaoDTO;
import com.energ_ia.api.dto.avaliacao.AnaliseResponseDTO;
import com.energ_ia.api.service.AnaliseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analise-energetica")
public class AnaliseController {

    @Autowired
    private AnaliseService analiseService;

    // Endpoint obrigatório (público) - não salva dados
    @PostMapping
    public ResponseEntity<AnaliseResponseDTO> analisar(@RequestBody AnaliseRequisicaoDTO request) {
        AnaliseResponseDTO response = analiseService.analisarConsumo(request);
        return ResponseEntity.ok(response);
    }
}
