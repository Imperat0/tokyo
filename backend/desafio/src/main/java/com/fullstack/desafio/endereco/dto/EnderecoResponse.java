package com.fullstack.desafio.endereco.dto;

import java.time.Instant;

public record EnderecoResponse(
        Long id,
        String rua,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String estado,
        String cep,
        Long usuarioId,
        Instant createdAt,
        Instant updatedAt
) {
}