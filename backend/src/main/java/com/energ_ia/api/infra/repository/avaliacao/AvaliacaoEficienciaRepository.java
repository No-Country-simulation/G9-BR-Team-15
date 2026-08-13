package com.energ_ia.api.infra.repository.avaliacao;

import com.energ_ia.api.domain.avaliacao.AvaliacaoEficiencia;
import com.energ_ia.api.domain.cliente.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AvaliacaoEficienciaRepository extends JpaRepository<AvaliacaoEficiencia, Long> {
    Optional<AvaliacaoEficiencia> findByClienteAndMesReferencia(Cliente cliente, LocalDate mesAtual);

    @Query("""
        SELECT a FROM AvaliacaoEficiencia a 
        WHERE a.mesReferencia = (
            SELECT MAX(sub.mesReferencia) 
            FROM AvaliacaoEficiencia sub 
            WHERE sub.cliente.id = a.cliente.id
        )
    """)
    List<AvaliacaoEficiencia> buscarUltimaAvaliacaoDeTodosClientes();
}
