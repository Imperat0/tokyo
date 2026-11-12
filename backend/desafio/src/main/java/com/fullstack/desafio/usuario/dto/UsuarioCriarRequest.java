package com.fullstack.desafio.usuario.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioCriarRequest(

        @Schema(description = "Nome completo", example = "Ana Silva", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
        String nome,

        @Schema(description = "E-mail único usado para login", example = "ana@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 180, message = "O e-mail deve ter no máximo 180 caracteres")
        String email,

        @Schema(description = "Senha com 8 a 72 caracteres", example = "MinhaSenha123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
        String senha
) {
}
