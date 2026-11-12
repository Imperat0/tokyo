package com.fullstack.desafio.controller;

import com.fullstack.desafio.auth.AuthService;
import com.fullstack.desafio.auth.dto.LoginRequest;
import com.fullstack.desafio.auth.dto.CsrfTokenResponse;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Login e emissão de token JWT")
public class AuthController {

    private final AuthService authService;

    @Value("${app.auth.cookie.name:ACCESS_TOKEN}")
    private String cookieName;

    @Value("${app.auth.cookie.secure:false}")
    private boolean secureCookie;

    @Value("${app.auth.cookie.same-site:Lax}")
    private String sameSite;

    @GetMapping("/csrf")
    @Operation(summary = "Obter token CSRF", description = "Prepara o cookie CSRF para operações que alteram dados.")
    public ResponseEntity<CsrfTokenResponse> csrf(CsrfToken csrfToken) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new CsrfTokenResponse(csrfToken.getToken()));
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário", description = "Valida e-mail e senha e grava o JWT em cookie HttpOnly.")
    @SecurityRequirement(name = "csrfTokenAuth")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest request) {
        var token = authService.login(request);
        return ResponseEntity.noContent()
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.SET_COOKIE, authCookie(token.value(), token.expiresIn()).toString())
                .build();
    }

    @PostMapping("/logout")
    @Operation(summary = "Encerrar sessão", description = "Remove o cookie de autenticação.")
    @SecurityRequirement(name = "csrfTokenAuth")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent()
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.SET_COOKIE, authCookie("", 0).toString())
                .build();
    }

    private ResponseCookie authCookie(String token, long maxAgeSeconds) {
        return ResponseCookie.from(cookieName, token)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite(sameSite)
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
    }
}
