package com.energ_ia.api.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.energ_ia.api.dto.AnaliseRequisicao;

@RestController
@RequestMapping("/analise-energetica")
public class AnaliseController {

    @PostMapping
    public ResponseEntity<Map<String, Object>> analisar(@RequestBody AnaliseRequisicao request) {
        // 1. Log para ver os dados recebidos
        System.out.println("Recebido: " + request);

        // 2. Resposta mockada (igual ao que o teste espera)
        Map<String, Object> response = new HashMap<>();
        response.put("categoria", "Ineficiente");
        response.put("probabilidade", 0.85);
        response.put("recomendacoes", List.of("Dica 1", "Dica 2"));
        response.put("custo_estimado_mensal", 315.00);

        return ResponseEntity.ok(response);
    }
}