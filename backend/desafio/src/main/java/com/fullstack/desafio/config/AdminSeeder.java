package com.fullstack.desafio.config;

import com.fullstack.desafio.usuario.Perfil;
import com.fullstack.desafio.usuario.Usuario;
import com.fullstack.desafio.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.name:Administrador}")
    private String name;

    @Value("${app.admin.email:admin@teste.com}")
    private String email;

    @Value("${app.admin.password}")
    private String password;

    @Override
    public void run(ApplicationArguments args) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (usuarioRepository.existsByEmail(normalizedEmail)) {
            return;
        }

        Usuario admin = new Usuario();
        admin.setNome(name.trim());
        admin.setEmail(normalizedEmail);
        admin.setSenha(passwordEncoder.encode(password));
        admin.setPerfil(Perfil.ADMIN);
        usuarioRepository.save(admin);
    }
}
