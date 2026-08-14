package com.energ_ia.api.infra.repository.equipamento;

import com.energ_ia.api.domain.equipamento.EquipamentoCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EquipamentoRepository extends JpaRepository<EquipamentoCatalogo, Long> {

    boolean existsByTipoAndMarcaAndModelo(String tipo, String marca, String modelo);
}
