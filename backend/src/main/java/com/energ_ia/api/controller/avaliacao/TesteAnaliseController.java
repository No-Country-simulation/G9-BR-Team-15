package com.energ_ia.api.controller.avaliacao;

import com.energ_ia.api.dto.avaliacao.TesteAnaliseRequisicaoDTO;
import com.energ_ia.api.dto.avaliacao.TesteAnaliseResponseDTO;
import com.energ_ia.api.service.avaliacao.TesteAnaliseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/teste")
public class TesteAnaliseController {

    @Autowired
    private TesteAnaliseService analiseService;

    @PostMapping("/analise-energetica")
    public ResponseEntity<TesteAnaliseResponseDTO> analisar(@RequestBody Map<String, Object> requestBody) {
        TesteAnaliseRequisicaoDTO request = mapearRequest(requestBody);
        TesteAnaliseResponseDTO response = analiseService.analisarConsumo(request);
        return ResponseEntity.ok(response);
    }

    private TesteAnaliseRequisicaoDTO mapearRequest(Map<String, Object> requestBody) {
        Integer consumoKwh = lerInteiro(requestBody, "consumo_kwh", "consumoKwh");
        Boolean usoHorarioPico = lerBooleano(requestBody, "uso_horario_pico", "usoHorarioPico");
        Integer quantidadeEquipamentos = lerInteiro(requestBody, "quantidade_equipamentos", "quantidadeEquipamentos");
        String tipoImovel = lerTexto(requestBody, "tipo_imovel", "tipoImovel");
        Integer horasAltoConsumo = lerInteiro(requestBody, "horas_alto_consumo", "horasAltoConsumo");

        return new TesteAnaliseRequisicaoDTO(consumoKwh, usoHorarioPico, quantidadeEquipamentos, tipoImovel, horasAltoConsumo);
    }

    private Integer lerInteiro(Map<String, Object> requestBody, String... campos) {
        for (String campo : campos) {
            Object valor = requestBody.get(campo);
            if (valor instanceof Number numero) {
                return numero.intValue();
            }
            if (valor instanceof String texto && !texto.isBlank()) {
                return Integer.parseInt(texto);
            }
        }
        return null;
    }

    private Boolean lerBooleano(Map<String, Object> requestBody, String... campos) {
        for (String campo : campos) {
            Object valor = requestBody.get(campo);
            if (valor instanceof Boolean bool) {
                return bool;
            }
            if (valor instanceof String texto) {
                return Boolean.parseBoolean(texto);
            }
        }
        return null;
    }

    private String lerTexto(Map<String, Object> requestBody, String... campos) {
        for (String campo : campos) {
            Object valor = requestBody.get(campo);
            if (valor instanceof String texto && !texto.isBlank()) {
                return texto;
            }
        }
        return null;
    }
}
