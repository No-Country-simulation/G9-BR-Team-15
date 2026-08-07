package com.energ_ia.api.infra.repository.cliente;

import com.energ_ia.api.domain.cliente.ClienteEquipamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteEquipamentoRepository extends JpaRepository<ClienteEquipamento, Long> {
}
