package com.energ_ia.api.infra.repository.consumo;

import com.energ_ia.api.domain.cliente.Cliente;
import com.energ_ia.api.domain.consumo.ConsumoMensal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConsumoMensalRepository extends JpaRepository<ConsumoMensal, Long> {
    Optional<ConsumoMensal> findByClienteAndMesReferencia(Cliente cliente, LocalDate mesAtual);

    List<ConsumoMensal> findByClienteId(Long clienteId);

    boolean existsByClienteIdAndMesReferencia(Long clienteId, LocalDate mesNormalizado);
}
