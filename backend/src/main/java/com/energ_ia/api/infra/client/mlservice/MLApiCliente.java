package com.energ_ia.api.infra.client.mlservice;

import com.energ_ia.api.dto.ml.MLApiRequestDTO;
import com.energ_ia.api.dto.ml.MLApiResponseDTO;
import com.energ_ia.api.dto.avaliacao.TesteAnaliseRequestDTO;
import com.energ_ia.api.dto.avaliacao.TesteAnaliseResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Component
public class MLApiCliente {

    private static final Logger log = LoggerFactory.getLogger(MLApiCliente.class);

    @Autowired
    private RestTemplate restTemplate;

    @Value("${DB_API_OBRIGATORIA}")
    private String urlObrigatoria;

    @Value("${DB_API_USUARIO_LOGADO}")
    private String urlUsuarioLogado;


    public MLApiResponseDTO chamarApiUsuarioLogado(MLApiRequestDTO request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<MLApiRequestDTO> entity = new HttpEntity<>(request, headers);

        try {
            log.info("Chamando ML API Oficial: {}", urlUsuarioLogado);
            ResponseEntity<MLApiResponseDTO> response = restTemplate.postForEntity(
                    urlUsuarioLogado, entity, MLApiResponseDTO.class);

            return response.getBody();
        } catch (Exception e) {
            log.error("Erro ao chamar ML API Oficial: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao comunicar com serviço de análise oficial: " + e.getMessage(), e);
        }
    }

    public TesteAnaliseResponseDTO chamarApiObrigatoria(TesteAnaliseRequestDTO request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("consumo_kwh", request.consumoKwh() != null ? request.consumoKwh().doubleValue() : 0.0);
        payload.put("uso_horario_pico", request.usoHorarioPico() != null ? request.usoHorarioPico() : false);
        payload.put("quantidade_equipamentos", request.quantidadeEquipamentos() != null ? request.quantidadeEquipamentos() : 0);
        payload.put("tipo_imovel", request.tipoImovel() != null ? request.tipoImovel() : "RESIDENCIAL");
        payload.put("horas_alto_consumo", request.horasAltoConsumo() != null ? request.horasAltoConsumo().doubleValue() : 0.0);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            log.info("Chamando ML API Teste: {}", urlObrigatoria);
            ResponseEntity<TesteAnaliseResponseDTO> response = restTemplate.postForEntity(
                    urlObrigatoria, entity, TesteAnaliseResponseDTO.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Erro ao chamar ML API Teste: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao comunicar com serviço de análise teste: " + e.getMessage(), e);
        }
    }
}