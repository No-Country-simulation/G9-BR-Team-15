package com.energ_ia.api.infra.repository.tarifa;

import com.energ_ia.api.domain.core.TipoPessoa;
import com.energ_ia.api.domain.tarifa.TarifaEnergia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TarifaEnergiaRepository extends JpaRepository<TarifaEnergia, Long> {

    @Query("SELECT t FROM TarifaEnergia t WHERE t.tipoPessoa = :tipoPessoa AND (t.dataFimVigencia IS NULL OR t.dataFimVigencia >= CURRENT_DATE)")
    Optional<TarifaEnergia> findTarifaVigente(@Param("tipoPessoa") TipoPessoa tipoPessoa);

}