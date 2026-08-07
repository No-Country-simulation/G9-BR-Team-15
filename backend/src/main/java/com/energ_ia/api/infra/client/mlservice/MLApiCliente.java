package com.energ_ia.api.domain.cliente;

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

import com.energ_ia.api.dto.avaliacao.AnaliseRequisicaoDTO;
import com.energ_ia.api.dto.avaliacao.AnaliseResponseDTO;
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

    public AnaliseResponseDTO chamarApiObrigatoria(AnaliseRequisicaoDTO request) {
        return chamar(urlObrigatoria, request);
    }

    public AnaliseResponseDTO chamarApiUsuarioLogado(AnaliseRequisicaoDTO request) {
        return chamar(urlUsuarioLogado, request);
    }

    private AnaliseResponseDTO chamar(String url, AnaliseRequisicaoDTO request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("tipo_pessoa", "PF");
        payload.put("tipo_imovel", request.tipoImovel() != null ? request.tipoImovel() : "Residencial");

        Map<String, Object> equipamento = new LinkedHashMap<>();
        equipamento.put("tipo", "Ar Condicionado Split");
        equipamento.put("quantidade", 1);
        equipamento.put("horas_uso_diario", 4.0);
        equipamento.put("dias_uso_mes", 30);
        payload.put("equipamentos", List.of(equipamento));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            log.info("Chamando ML API: {}", url);
            ResponseEntity<AnaliseResponseDTO> response = restTemplate.postForEntity(
                    url, entity, AnaliseResponseDTO.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Erro ao chamar ML API: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao comunicar com serviço de análise: " + e.getMessage(), e);
        }
    }
}
