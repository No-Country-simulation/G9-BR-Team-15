package com.energ_ia.api.infra.seeder;

import com.energ_ia.api.domain.cliente.Cliente;
import com.energ_ia.api.domain.core.TipoImovel;
import com.energ_ia.api.domain.core.TipoPessoa;
import com.energ_ia.api.domain.usuario.Usuario;
import com.energ_ia.api.infra.repository.cliente.ClienteRepository;
import com.energ_ia.api.infra.repository.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Profile({"dev", "local"})
@RequiredArgsConstructor
@Slf4j
public class DataRankingSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("[SEEDER] Verificando se o banco precisa de dados iniciais...");


        if (usuarioRepository.count() > 0) {
            log.info("[SEEDER] O banco já contém dados. Seed ignorado.");
            return;
        }

        log.info("[SEEDER] Populando banco de dados com massa de testes...");

        Usuario usuario = new Usuario();
        usuario.setNome("João da Silva");
        usuario.setEmail("joao@teste.com");
        usuario.setSenhaHash("$2a$12$ExemploDeHashBcryptParaTestesSomenteAki..."); // Senha mockada
        usuario.setCriadoEm(LocalDateTime.now());
        usuarioRepository.save(usuario);

        Cliente cliente = new Cliente();
        cliente.setUsuario(usuario);
        cliente.setNomeRazaoSocial("Residência do João");
        cliente.setTipoPessoa(TipoPessoa.PF);
        cliente.setTipoImovel(TipoImovel.RESIDENCIAL);
        cliente.setCep("24900-000");
        cliente.setCidade("Maricá");
        cliente.setEstado("RJ");
        cliente.setPais("Brasil");
        cliente.setAtivo(true);
        cliente.setCriadoEm(LocalDateTime.now());
        clienteRepository.save(cliente);

        log.info("[SEEDER] Massa de dados injetada com sucesso!");
    }
}