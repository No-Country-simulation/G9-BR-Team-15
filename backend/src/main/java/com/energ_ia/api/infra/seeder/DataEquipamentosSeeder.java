package com.energ_ia.api.infra.seeder;

import com.energ_ia.api.domain.equipamento.EquipamentoCatalogo;
import com.energ_ia.api.infra.repository.equipamento.EquipamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataEquipamentosSeeder {

    private final EquipamentoRepository equipamentoRepository;

    @Bean
    CommandLineRunner initDatabase() {
        return args -> {
            if (equipamentoRepository.count() == 0) {
                List<EquipamentoCatalogo> equipamentos = List.of(
                        new EquipamentoCatalogo("Geladeira Frost Free", "Padrao", "Padrao", 150),
                        new EquipamentoCatalogo("Micro-ondas", "Padrao", "Padrao", 1200),
                        new EquipamentoCatalogo("Airfryer", "Padrao", "Padrao", 1500),
                        new EquipamentoCatalogo("Forno Elétrico", "Padrao", "Padrao", 1800),
                        new EquipamentoCatalogo("Liquidificador", "Padrao", "Padrao", 400),
                        new EquipamentoCatalogo("Coifa / Depurador", "Padrao", "Padrao", 120),
                        new EquipamentoCatalogo("Freezer Horizontal", "Padrao", "Padrao", 300),
                        new EquipamentoCatalogo("Ar Condicionado Split", "Padrao", "Padrao", 1200),
                        new EquipamentoCatalogo("Ventilador de Coluna", "Padrao", "Padrao", 80),
                        new EquipamentoCatalogo("Chuveiro Elétrico", "Padrao", "Padrao", 5500),
                        new EquipamentoCatalogo("Torneira Elétrica", "Padrao", "Padrao", 4500),
                        new EquipamentoCatalogo("Televisão Smart", "Padrao", "Padrao", 100),
                        new EquipamentoCatalogo("Computador Desktop", "Padrao", "Padrao", 250),
                        new EquipamentoCatalogo("Videogame Console", "Padrao", "Padrao", 150),
                        new EquipamentoCatalogo("Robô Aspirador", "Padrao", "Padrao", 40),
                        new EquipamentoCatalogo("Servidores / TI", "Padrao", "Padrao", 2000),
                        new EquipamentoCatalogo("Maquinário Industrial", "Padrao", "Padrao", 8000),
                        new EquipamentoCatalogo("Iluminação Comercial (LEDs)", "Padrao", "Padrao", 1000)
                );

                equipamentoRepository.saveAll(equipamentos);
            }
        };
    }
}