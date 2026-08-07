package com.energ_ia.api.infra.repository.cliente;

import com.energ_ia.api.domain.cliente.Cliente;
import com.energ_ia.api.domain.usuario.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByUsuarioAndNomeRazaoSocial(Usuario usuario, String nomeRazaoSocial);

    List<Cliente> findByUsuario(Usuario usuario);
}
