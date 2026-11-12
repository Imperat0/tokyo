package com.fullstack.desafio.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiDocumentation() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Usuários e Endereços")
                        .description("""
                                API REST para gerenciamento de usuários e endereços.
                                O login grava o JWT em cookie HttpOnly. Antes de operações que alteram dados,
                                obtenha o token CSRF em /api/auth/csrf e envie-o no header X-XSRF-TOKEN.
                                Usuários comuns acessam apenas os próprios dados; operações administrativas
                                são restritas ao perfil ADMIN.
                                """)
                        .version("1.0.0"))
                .components(new Components()
                        .addSecuritySchemes("cookieAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("ACCESS_TOKEN")
                                .description("Cookie HttpOnly criado em POST /api/auth/login"))
                        .addSecuritySchemes("csrfTokenAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-XSRF-TOKEN")
                                .description("Token retornado por GET /api/auth/csrf")));
    }

    @Bean
    public OpenApiCustomizer authenticatedWriteRequirements() {
        return openApi -> {
            var paths = openApi.getPaths();

            var usuarios = paths.get("/api/usuarios/{id}");
            if (usuarios != null) {
                requireBoth(usuarios.getPut());
                requireBoth(usuarios.getDelete());
            }

            var criarEndereco = paths.get("/api/usuarios/{usuarioId}/enderecos");
            if (criarEndereco != null) {
                requireBoth(criarEndereco.getPost());
            }

            var enderecos = paths.get("/api/enderecos/{id}");
            if (enderecos != null) {
                requireBoth(enderecos.getPut());
                requireBoth(enderecos.getDelete());
            }
        };
    }

    private void requireBoth(io.swagger.v3.oas.models.Operation operation) {
        if (operation != null) {
            operation.setSecurity(List.of(new SecurityRequirement()
                    .addList("cookieAuth")
                    .addList("csrfTokenAuth")));
        }
    }
}
