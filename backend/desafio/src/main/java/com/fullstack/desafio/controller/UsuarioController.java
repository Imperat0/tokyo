package com.fullstack.desafio.controller;

import com.fullstack.desafio.service.UsuarioService;
import com.fullstack.desafio.shared.PageableSupport;
import com.fullstack.desafio.shared.PageResponse;
import com.fullstack.desafio.usuario.dto.UsuarioAtualizarRequest;
import com.fullstack.desafio.usuario.dto.UsuarioCriarRequest;
import com.fullstack.desafio.usuario.dto.UsuarioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Set;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Cadastro e gerenciamento de usuários")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    @Operation(summary = "Registrar usuário", description = "Cria um usuário comum. O perfil é sempre USER e não pode ser definido pelo cliente.")
    @SecurityRequirement(name = "csrfTokenAuth")
    public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody UsuarioCriarRequest request) {
        UsuarioResponse criado = usuarioService.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar usuários", description = "Lista usuários paginados. Apenas ADMIN. Ordenação por nome, email ou createdAt; size máximo 100.")
    @Parameter(name = "sort", in = ParameterIn.QUERY,
            description = "Ordenação por nome, email ou createdAt. Repita o parâmetro para múltiplos campos.",
            example = "createdAt,desc")
    @SecurityRequirement(name = "cookieAuth")
    public PageResponse<UsuarioResponse> listar(
            @RequestParam(defaultValue = "0") String page,
            @RequestParam(defaultValue = "20") String size,
            HttpServletRequest request) {
        var pageable = PageableSupport.create(
                page,
                size,
                request.getParameterValues("sort"),
                Sort.by(Sort.Order.asc("nome")),
                Set.of("nome", "email", "createdAt"));
        return usuarioService.buscarTodos(pageable);
    }

    @GetMapping("/me")
    @Operation(summary = "Buscar usuário autenticado", description = "Retorna o perfil associado ao token JWT.")
    @SecurityRequirement(name = "cookieAuth")
    public UsuarioResponse buscarUsuarioAutenticado(Authentication authentication) {
        return usuarioService.buscarPorEmail(authentication.getName());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityAuthorization.isUser(authentication, #p0)")
    @Operation(summary = "Buscar usuário", description = "ADMIN pode buscar qualquer usuário; usuário comum só pode buscar o próprio cadastro.")
    @SecurityRequirement(name = "cookieAuth")
    public UsuarioResponse buscarPorId(@PathVariable Long id) {
        return usuarioService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityAuthorization.isUser(authentication, #p0)")
    @Operation(summary = "Atualizar usuário", description = "ADMIN pode atualizar qualquer usuário; usuário comum só pode atualizar os próprios dados.")
    @SecurityRequirement(name = "csrfTokenAuth")
    public UsuarioResponse atualizar(@PathVariable Long id, @Valid @RequestBody UsuarioAtualizarRequest request) {
        return usuarioService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityAuthorization.isUser(authentication, #p0)")
    @Operation(summary = "Excluir usuário", description = "ADMIN pode excluir qualquer usuário; usuário comum só pode excluir a própria conta.")
    @SecurityRequirement(name = "csrfTokenAuth")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        usuarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
