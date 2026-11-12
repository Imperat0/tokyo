package com.fullstack.desafio.security;

import com.fullstack.desafio.endereco.EnderecoRepository;
import com.fullstack.desafio.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityAuthorization {

    private final UsuarioRepository usuarioRepository;
    private final EnderecoRepository enderecoRepository;

    public boolean isUser(Authentication authentication, Long usuarioId) {
        return usuarioRepository.existsByEmailAndId(authentication.getName(), usuarioId);
    }

    public boolean ownsAddress(Authentication authentication, Long enderecoId) {
        return enderecoRepository.existsByIdAndUsuario_Email(enderecoId, authentication.getName());
    }
}
