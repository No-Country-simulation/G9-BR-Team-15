package com.energ_ia.api.controller;

import com.energ_ia.api.dto.avaliacao.TesteAnaliseRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AnaliseControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Deve aceitar payloads em camelCase e mapear para o DTO")
    public void deveAceitarPayloadEmCamelCase() throws Exception {
        String jsonRequest = """
                {
                  "consumoKwh": 420,
                  "usoHorarioPico": true,
                  "quantidadeEquipamentos": 10,
                  "tipoImovel": "Casa",
                  "horasAltoConsumo": 8
                }
                """;

        TesteAnaliseRequestDTO dto = objectMapper.readValue(jsonRequest, TesteAnaliseRequestDTO.class);

        assertNotNull(dto);
        assertEquals(420, dto.consumoKwh());
        assertEquals(true, dto.usoHorarioPico());
        assertEquals(10, dto.quantidadeEquipamentos());
        assertEquals("Casa", dto.tipoImovel());
        assertEquals(8, dto.horasAltoConsumo());
    }
}
