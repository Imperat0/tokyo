package com.fullstack.desafio.usuario.dto;

import com.fullstack.desafio.usuario.Perfil;

import java.time.Instant;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        Perfil perfil,
        Instant createdAt,
        Instant updatedAt
) {
}
