package com.energ_ia.api.service.usuario;

import com.energ_ia.api.domain.usuario.Usuario;
import com.energ_ia.api.dto.usuario.LoginRequestDTO;
import com.energ_ia.api.dto.usuario.RegisterRequestDTO;
import com.energ_ia.api.dto.usuario.AuthResponseDTO;
import com.energ_ia.api.infra.repository.usuario.UsuarioRepository;
import com.energ_ia.api.infra.security.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final TokenService tokenService;

    public AuthResponseDTO cadastrar(RegisterRequestDTO request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email já cadastrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setSenhaHash(passwordEncoder.encode(request.senha()));

        usuario = usuarioRepository.save(usuario);

        return new AuthResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                "Usuário cadastrado com sucesso",
                null // No cadastro não devolvemos token
        );
    }

    public AuthResponseDTO login(LoginRequestDTO request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!passwordEncoder.matches(request.senha(), usuario.getSenhaHash())) {
            throw new RuntimeException("Senha incorreta");
        }

        String token = tokenService.gerarToken(usuario);

        return new AuthResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                "Login realizado com sucesso",
                token

        );
    }
}
