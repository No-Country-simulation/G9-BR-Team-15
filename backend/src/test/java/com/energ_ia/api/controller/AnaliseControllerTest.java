package com.energ_ia.api.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnaliseController.class)
public class AnaliseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Deve receber dados de consumo e retornar a categoria Ineficiente com estimativa de R$ 315.00")
    public void deveAnalisarConsumoERetornarIneficiente() throws Exception {

        // 1. GIVEN (Dado): O JSON de entrada obrigatório definido no hackthon
        String jsonRequest = """
                {
                  "consumo_kwh": 420,
                  "uso_horario_pico": true,
                  "quantidade_equipamentos": 10,
                  "tipo_imovel": "Casa",
                  "horas_alto_consumo": 8
                }
                """;

        // 2. WHEN (Quando): Disparamos um POST para o endpoint
        var response = mockMvc.perform(post("/analise-energetica")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest));

        // 3. THEN (Então): Validamos se a resposta bate com o esperado
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.categoria").value("Ineficiente"))
                .andExpect(jsonPath("$.probabilidade").isNumber())
                .andExpect(jsonPath("$.recomendacoes").isArray())
                .andExpect(jsonPath("$.recomendacoes").isNotEmpty())
                .andExpect(jsonPath("$.custo_estimado_mensal").value(315.00));
    }
}