package com.fullstack.desafio.controller;

import com.fullstack.desafio.endereco.dto.EnderecoRequest;
import com.fullstack.desafio.endereco.dto.EnderecoResponse;
import com.fullstack.desafio.service.EnderecoService;
import com.fullstack.desafio.shared.PageableSupport;
import com.fullstack.desafio.shared.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Endereços", description = "Cadastro e gerenciamento de endereços vinculados a usuários")
public class EnderecoController {

    private final EnderecoService enderecoService;

    @PostMapping("/usuarios/{usuarioId}/enderecos")
    @PreAuthorize("hasRole('ADMIN') or @securityAuthorization.isUser(authentication, #p0)")
    @Operation(summary = "Cadastrar endereço", description = "Consulta o CEP no ViaCEP e usa logradouro, bairro, cidade e UF retornados. Informe o número e, opcionalmente, o complemento.")
    @SecurityRequirement(name = "csrfTokenAuth")
    public ResponseEntity<EnderecoResponse> criar(@PathVariable Long usuarioId,
                                                  @Valid @RequestBody EnderecoRequest request) {
        EnderecoResponse criado = enderecoService.criar(usuarioId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/enderecos/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @GetMapping("/usuarios/{usuarioId}/enderecos")
    @PreAuthorize("hasRole('ADMIN') or @securityAuthorization.isUser(authentication, #p0)")
    @Operation(summary = "Listar endereços do usuário", description = "ADMIN pode consultar qualquer usuário; usuário comum apenas os próprios endereços.")
    @Parameter(name = "sort", in = ParameterIn.QUERY,
            description = "Ordenação por id, rua, numero, bairro, cidade, estado, cep ou createdAt; size máximo 100.",
            example = "createdAt,desc")
    @SecurityRequirement(name = "cookieAuth")
    public PageResponse<EnderecoResponse> listarPorUsuario(
            @PathVariable Long usuarioId,
            @RequestParam(defaultValue = "0") String page,
            @RequestParam(defaultValue = "20") String size,
            HttpServletRequest request) {
        var pageable = criarPaginacao(page, size, request);
        return enderecoService.buscarPorUsuario(usuarioId, pageable);
    }

    @GetMapping("/enderecos")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos os endereços", description = "Lista endereços paginados. Disponível apenas para ADMIN.")
    @Parameter(name = "sort", in = ParameterIn.QUERY,
            description = "Ordenação por id, rua, numero, bairro, cidade, estado, cep ou createdAt; size máximo 100.",
            example = "createdAt,desc")
    @SecurityRequirement(name = "cookieAuth")
    public PageResponse<EnderecoResponse> listarTodos(
            @RequestParam(defaultValue = "0") String page,
            @RequestParam(defaultValue = "20") String size,
            HttpServletRequest request) {
        var pageable = criarPaginacao(page, size, request);
        return enderecoService.buscarTodos(pageable);
    }

    @GetMapping("/enderecos/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityAuthorization.ownsAddress(authentication, #p0)")
    @Operation(summary = "Buscar endereço", description = "ADMIN pode buscar qualquer endereço; usuário comum apenas endereços próprios.")
    @SecurityRequirement(name = "cookieAuth")
    public EnderecoResponse buscarPorId(@PathVariable Long id) {
        return enderecoService.buscarPorId(id);
    }

    @PutMapping("/enderecos/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityAuthorization.ownsAddress(authentication, #p0)")
    @Operation(summary = "Atualizar endereço", description = "Valida o CEP no ViaCEP. ADMIN pode atualizar qualquer endereço; usuário comum apenas endereços próprios.")
    @SecurityRequirement(name = "csrfTokenAuth")
    public EnderecoResponse atualizar(@PathVariable Long id, @Valid @RequestBody EnderecoRequest request) {
        return enderecoService.atualizar(id, request);
    }

    @DeleteMapping("/enderecos/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityAuthorization.ownsAddress(authentication, #p0)")
    @Operation(summary = "Excluir endereço", description = "ADMIN pode excluir qualquer endereço; usuário comum apenas endereços próprios.")
    @SecurityRequirement(name = "csrfTokenAuth")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        enderecoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    private org.springframework.data.domain.Pageable criarPaginacao(
            String page, String size, HttpServletRequest request) {
        return PageableSupport.create(
                page,
                size,
                request.getParameterValues("sort"),
                Sort.by(Sort.Order.desc("createdAt")),
                Set.of("id", "rua", "numero", "bairro", "cidade", "estado", "cep", "createdAt"));
    }
}