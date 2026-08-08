package com.energ_ia.api.infra.repository.avaliacao;

import com.energ_ia.api.domain.avaliacao.AvaliacaoEficiencia;
import com.energ_ia.api.domain.cliente.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface AvaliacaoEficienciaRepository extends JpaRepository<AvaliacaoEficiencia, Long> {
    Optional<AvaliacaoEficiencia> findByClienteAndMesReferencia(Cliente cliente, LocalDate mesAtual);
}
