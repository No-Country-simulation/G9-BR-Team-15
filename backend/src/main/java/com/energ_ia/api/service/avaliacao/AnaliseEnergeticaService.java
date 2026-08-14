package com.energ_ia.api.service.avaliacao;

import com.energ_ia.api.domain.avaliacao.AvaliacaoEficiencia;
import com.energ_ia.api.domain.consumo.ConsumoMensal;
import com.energ_ia.api.domain.cliente.Cliente;
import com.energ_ia.api.domain.core.CategoriaEficiencia;
import com.energ_ia.api.domain.tarifa.TarifaEnergia;
import com.energ_ia.api.dto.avaliacao.*;
import com.energ_ia.api.dto.ml.MLApiRequestDTO;
import com.energ_ia.api.dto.ml.MLApiResponseDTO;
import com.energ_ia.api.infra.client.mlservice.MLApiCliente;
import com.energ_ia.api.infra.repository.avaliacao.AvaliacaoEficienciaRepository;
import com.energ_ia.api.infra.repository.consumo.ConsumoMensalRepository;
import com.energ_ia.api.infra.repository.cliente.ClienteRepository;
import com.energ_ia.api.infra.repository.tarifa.TarifaEnergiaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnaliseEnergeticaService {

    private final ClienteRepository clienteRepository;
    private final ConsumoMensalRepository consumoRepository;
    private final AvaliacaoEficienciaRepository avaliacaoRepository;
    private final TarifaEnergiaRepository tarifaRepository;
    private final MLApiCliente mlApiCliente;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public AnaliseResponseDTO gerarAnaliseCompleta(Long clienteId) {

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado!"));

        if (cliente.getEquipamentos() == null || cliente.getEquipamentos().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O cliente não possui equipamentos cadastrados.");
        }

        double valorKwh = tarifaRepository.findTarifaVigente(cliente.getTipoPessoa())
                .map(TarifaEnergia::getValorKwh)
                .orElse(0.750);

        double consumoPrevistoKwh = calcularConsumoMatematico(cliente);
        double custoPrevistoLocal = consumoPrevistoKwh * valorKwh;

        List<MLApiRequestDTO.MLEquipamentoDTO> equipamentosIA = cliente.getEquipamentos().stream()
                .map(ce -> new MLApiRequestDTO.MLEquipamentoDTO(
                        ce.getEquipamentoCatalogo().getTipo(),
                        ce.getQuantidade(),
                        ce.getHorasUsoDiario(),
                        ce.getDiasUsoMes()
                )).toList();

        MLApiRequestDTO requestIA = new MLApiRequestDTO(
                cliente.getTipoPessoa().name(),
                cliente.getTipoImovel().name(),
                equipamentosIA
        );

        MLApiResponseDTO respostaIA = mlApiCliente.chamarApiUsuarioLogado(requestIA);

        LocalDate mesAtual = LocalDate.now().withDayOfMonth(1);

        ConsumoMensal consumo = consumoRepository.findByClienteAndMesReferencia(cliente, mesAtual)
                .orElseGet(() -> {
                    ConsumoMensal novoConsumo = new ConsumoMensal();
                    novoConsumo.setCliente(cliente);
                    novoConsumo.setMesReferencia(mesAtual);
                    return novoConsumo;
                });

        consumo.setConsumoPrevistoKwh(consumoPrevistoKwh);
        consumo.setConsumoEstimadoIaKwh(respostaIA.consumoEstimadoKwh());
        consumoRepository.save(consumo);

        AvaliacaoEficiencia avaliacao = avaliacaoRepository.findByClienteAndMesReferencia(cliente, mesAtual)
                .orElseGet(() -> {
                    AvaliacaoEficiencia novaAvaliacao = new AvaliacaoEficiencia();
                    novaAvaliacao.setCliente(cliente);
                    novaAvaliacao.setMesReferencia(mesAtual);
                    return novaAvaliacao;
                });

        avaliacao.setCategoriaEficiencia(CategoriaEficiencia.valueOf(respostaIA.categoria().toUpperCase()));
        avaliacao.setScoreSustentabilidade((int) (respostaIA.probabilidade() * 100));

        try {
            String dicasJson = objectMapper.writeValueAsString(respostaIA.recomendacoes());
            avaliacao.setDicasMelhoria(dicasJson);
        } catch (JsonProcessingException e) {
            avaliacao.setDicasMelhoria("[]");
        }

        avaliacaoRepository.save(avaliacao);

        return new AnaliseResponseDTO(
                avaliacao.getId(),
                consumoPrevistoKwh,
                custoPrevistoLocal,
                respostaIA.categoria(),
                avaliacao.getScoreSustentabilidade(),
                respostaIA.recomendacoes(),
                respostaIA.alertaConsumoAlto()
        );
    }

    private double calcularConsumoMatematico(Cliente cliente) {
        return cliente.getEquipamentos().stream()
                .mapToDouble(ce -> {
                    double potenciaKw = ce.getEquipamentoCatalogo().getPotenciaWatts() / 1000.0;
                    double horasUso = ce.getHorasUsoDiario().doubleValue();

                    return potenciaKw * horasUso * ce.getDiasUsoMes() * ce.getQuantidade();
                })
                .sum();
    }
}