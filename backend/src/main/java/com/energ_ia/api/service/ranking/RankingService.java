package com.energ_ia.api.service.ranking;

import com.energ_ia.api.domain.avaliacao.AvaliacaoEficiencia;
import com.energ_ia.api.domain.cliente.Cliente;
import com.energ_ia.api.domain.core.StatusProcessamento;
import com.energ_ia.api.domain.ranking.RankingGlobal;
import com.energ_ia.api.domain.ranking.RankingMetadata;
import com.energ_ia.api.domain.ranking.TipoRanking;
import com.energ_ia.api.dto.ranking.RankingGeralResponseDTO;
import com.energ_ia.api.dto.ranking.RankingResponseDTO;
import com.energ_ia.api.infra.repository.avaliacao.AvaliacaoEficienciaRepository;
import com.energ_ia.api.infra.repository.ranking.RankingMetadataRepository;
import com.energ_ia.api.infra.repository.ranking.RankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final RankingRepository rankingRepository;
    private final RankingMetadataRepository metadataRepository;
    private final AvaliacaoEficienciaRepository avaliacaoRepository;

    public List<RankingResponseDTO> obterTop10(TipoRanking tipoRanking) {
        return rankingRepository.findTop10ByTipoRankingOrderByPosicaoAsc(tipoRanking).stream()
                .map(this::mapearParaDTO)
                .toList();
    }

    public RankingResponseDTO obterPosicaoCliente(Long clienteId, TipoRanking tipoRanking) {
        RankingGlobal ranking = rankingRepository.findByClienteIdAndTipoRanking(clienteId, tipoRanking)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Posição não encontrada para este cliente no ranking especificado."));

        return mapearParaDTO(ranking);
    }

    private RankingResponseDTO mapearParaDTO(RankingGlobal r) {
        return new RankingResponseDTO(
                r.getId(),
                r.getCliente().getId(),
                r.getNomeRazaoSocial(),
                r.getLocalidade(),
                r.getTipoImovel(),
                r.getTipoRanking(),
                r.getPosicao(),
                r.getPontuacao(),
                r.getCategoriaEficiencia(),
                r.getAtualizadoEm()
        );
    }

    @Transactional
    public void calcularEAtualizarRanking() {
        atualizarMetadata("AtualizacaoDiariaRanking", StatusProcessamento.PROCESSANDO);

        try {
            for (TipoRanking tipo : TipoRanking.values()) {
                processarRankingPorTipo(tipo);
            }

            LocalDateTime agora = LocalDateTime.now();
            atualizarMetadataSucesso("AtualizacaoDiariaRanking", agora, agora.plusDays(1).withHour(23).withMinute(0).withSecond(0));

        } catch (Exception e) {
            atualizarMetadataErro("AtualizacaoDiariaRanking", e.getMessage());
            throw e;
        }
    }

    private void atualizarMetadata(String nomeJob, StatusProcessamento status) {
        RankingMetadata meta = metadataRepository.findByJob(nomeJob).orElse(new RankingMetadata());
        meta.setJob(nomeJob);
        meta.setStatus(status);
        meta.setUltimaAtualizacao(LocalDateTime.now());
        metadataRepository.save(meta);
    }

    private void atualizarMetadataSucesso(String nomeJob, LocalDateTime ultima, LocalDateTime proxima) {
        RankingMetadata meta = metadataRepository.findByJob(nomeJob).orElse(new RankingMetadata());
        meta.setJob(nomeJob);
        meta.setStatus(StatusProcessamento.CONCLUIDO);
        meta.setUltimaAtualizacao(ultima);
        meta.setProximaAtualizacao(proxima);
        metadataRepository.save(meta);
    }

    private void atualizarMetadataErro(String nomeJob, String erro) {
        RankingMetadata meta = metadataRepository.findByJob(nomeJob).orElse(new RankingMetadata());
        meta.setJob(nomeJob);
        meta.setStatus(StatusProcessamento.ERRO);
        metadataRepository.save(meta);
    }

    @Transactional
    public void processarRankingPorTipo(TipoRanking tipoRanking) {
        List<AvaliacaoEficiencia> avaliacoes = avaliacaoRepository.buscarUltimaAvaliacaoDeTodosClientes();

        java.util.Map<String, List<AvaliacaoEficiencia>> agrupadoPorLocalidade = avaliacoes.stream()
                .collect(java.util.stream.Collectors.groupingBy(a -> extrairLocalidadePorTipo(a.getCliente(), tipoRanking)));

        for (java.util.Map.Entry<String, List<AvaliacaoEficiencia>> entry : agrupadoPorLocalidade.entrySet()) {
            String localidade = entry.getKey();
            List<AvaliacaoEficiencia> listaClientesNaLocalidade = entry.getValue();

            listaClientesNaLocalidade.sort((a1, a2) -> a2.getScoreSustentabilidade().compareTo(a1.getScoreSustentabilidade()));

            int posicao = 1;
            for (AvaliacaoEficiencia avaliacao : listaClientesNaLocalidade) {
                Cliente cliente = avaliacao.getCliente();

                RankingGlobal ranking = rankingRepository.findByClienteIdAndTipoRanking(cliente.getId(), tipoRanking)
                        .orElse(null);

                if (ranking == null) {
                    ranking = new RankingGlobal();
                }

                ranking.setCliente(cliente);
                ranking.setLocalidade(localidade);
                ranking.setNomeRazaoSocial(cliente.getNomeRazaoSocial());
                ranking.setTipoImovel(cliente.getTipoImovel());
                ranking.setTipoRanking(tipoRanking);
                ranking.setPosicao(posicao++);
                ranking.setPontuacao(avaliacao.getScoreSustentabilidade());
                ranking.setCategoriaEficiencia(avaliacao.getCategoriaEficiencia());
                ranking.setAtualizadoEm(LocalDateTime.now());

                rankingRepository.saveAndFlush(ranking);
            }
        }
    }


    private String extrairLocalidadePorTipo(Cliente cliente, TipoRanking tipoRanking) {
        return switch (tipoRanking) {
            case PAIS -> cliente.getPais() != null ? cliente.getPais() : "Brasil";
            case ESTADO -> cliente.getEstado() != null ? cliente.getEstado() : "RJ";
            case CIDADE -> cliente.getCidade() != null ? cliente.getCidade() : "Maricá";
        };
    }

    public List<RankingGeralResponseDTO> obterTop10Publico(TipoRanking tipoRanking) {
        return rankingRepository.findTop10ByTipoRankingOrderByPosicaoAsc(tipoRanking).stream()
                .map(this::mapearParaRankingGeralDTO)
                .toList();
    }

    private RankingGeralResponseDTO mapearParaRankingGeralDTO(RankingGlobal r) {
        return new RankingGeralResponseDTO(
                r.getPosicao(),
                r.getNomeRazaoSocial(),
                r.getLocalidade(),
                r.getTipoImovel(),
                r.getPontuacao(),
                r.getCategoriaEficiencia(),
                r.getAtualizadoEm()
        );
    }
}