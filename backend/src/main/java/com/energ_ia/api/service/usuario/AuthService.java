package com.energ_ia.api.service.usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.energ_ia.api.domain.usuario.Usuario;
import com.energ_ia.api.dto.usuario.AuthResponseDTO;
import com.energ_ia.api.dto.usuario.LoginRequestDTO;
import com.energ_ia.api.dto.usuario.RegisterRequestDTO;
import com.energ_ia.api.infra.repository.usuario.UsuarioRepository;
@Service
public class AuthService {
    @Autowired
    private UsuarioRepository usuarioRepository;
    public AuthResponseDTO cadastrar(RegisterRequestDTO request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado");       }
        Usuario usuario = new Usuario();
        usuario.setNome(request.getNome());
        usuario.setEmail(request.getEmail());
        usuario.setSenhaHash(request.getSenha());
        usuario = usuarioRepository.save(usuario);
        return new AuthResponseDTO(usuario.getId(), usuario.getNome(), usuario.getEmail(), "Usuário cadastrado com sucesso");
    }
    public AuthResponseDTO login(LoginRequestDTO request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        if (!usuario.getSenhaHash().equals(request.getSenha())) {
            throw new RuntimeException("Senha incorreta");
        }
        return new AuthResponseDTO(usuario.getId(), usuario.getNome(), usuario.getEmail(), "Login realizado com sucesso");
    }
}
