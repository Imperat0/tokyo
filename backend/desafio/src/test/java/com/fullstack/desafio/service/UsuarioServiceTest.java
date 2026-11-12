package com.fullstack.desafio.service;

import com.fullstack.desafio.exception.EmailAlreadyExistsException;
import com.fullstack.desafio.exception.ResourceNotFoundException;
import com.fullstack.desafio.usuario.Perfil;
import com.fullstack.desafio.usuario.Usuario;
import com.fullstack.desafio.usuario.UsuarioRepository;
import com.fullstack.desafio.usuario.dto.UsuarioCriarRequest;
import com.fullstack.desafio.usuario.mapper.UsuarioMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(usuarioRepository, new UsuarioMapper(), passwordEncoder);
    }

    @Test
    void criaUsuarioComEmailNormalizadoSenhaHashEPerfilPadrao() {
        when(usuarioRepository.existsByEmail("ana@example.com")).thenReturn(false);
        when(passwordEncoder.encode("senha-segura")).thenReturn("hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        usuarioService.criar(new UsuarioCriarRequest(" Ana ", " Ana@Example.com ", "senha-segura"));

        var captor = org.mockito.ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertEquals("ana@example.com", captor.getValue().getEmail());
        assertEquals("hash", captor.getValue().getSenha());
        assertEquals(Perfil.USER, captor.getValue().getPerfil());
    }

    @Test
    void rejeitaEmailJaCadastrado() {
        when(usuarioRepository.existsByEmail("ana@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class,
                () -> usuarioService.criar(new UsuarioCriarRequest("Ana", "ana@example.com", "senha-segura")));
    }

    @Test
    void buscaUsuarioInexistenteRetornaErroDeRecurso() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> usuarioService.buscarPorId(99L));
    }

    @Test
    void buscaUsuarioPeloEmailNormalizado() {
        Usuario usuario = new Usuario();
        usuario.setNome("Ana Silva");
        usuario.setEmail("ana@example.com");
        when(usuarioRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(usuario));

        var resposta = usuarioService.buscarPorEmail(" Ana@Example.com ");

        assertEquals("ana@example.com", resposta.email());
        assertEquals("Ana Silva", resposta.nome());
    }
}
