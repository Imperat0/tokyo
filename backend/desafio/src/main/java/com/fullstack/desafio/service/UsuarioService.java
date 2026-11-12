package com.fullstack.desafio.service;

import com.fullstack.desafio.exception.EmailAlreadyExistsException;
import com.fullstack.desafio.exception.ResourceNotFoundException;
import com.fullstack.desafio.shared.PageResponse;
import com.fullstack.desafio.usuario.Usuario;
import com.fullstack.desafio.usuario.UsuarioRepository;
import com.fullstack.desafio.usuario.dto.UsuarioAtualizarRequest;
import com.fullstack.desafio.usuario.dto.UsuarioCriarRequest;
import com.fullstack.desafio.usuario.dto.UsuarioResponse;
import com.fullstack.desafio.usuario.mapper.UsuarioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponse criar(UsuarioCriarRequest request) {
        String email = normalizarEmail(request.email());
        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        Usuario usuario = usuarioMapper.paraEntidade(request);
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(request.senha()));

        return usuarioMapper.paraResposta(usuarioRepository.save(usuario));
    }

    public PageResponse<UsuarioResponse> buscarTodos(Pageable pageable) {
        return PageResponse.from(usuarioRepository.findAll(pageable).map(usuarioMapper::paraResposta));
    }

    public UsuarioResponse buscarPorId(Long id) {
        return usuarioMapper.paraResposta(obterOuLancarExcecao(id));
    }

    public UsuarioResponse buscarPorEmail(String email) {
        String emailNormalizado = normalizarEmail(email);
        Usuario usuario = usuarioRepository.findByEmail(emailNormalizado)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", emailNormalizado));
        return usuarioMapper.paraResposta(usuario);
    }

    @Transactional
    public UsuarioResponse atualizar(Long id, UsuarioAtualizarRequest request) {
        Usuario usuario = obterOuLancarExcecao(id);

        String email = normalizarEmail(request.email());
        if (usuarioRepository.existsByEmailAndIdNot(email, id)) {
            throw new EmailAlreadyExistsException(email);
        }

        usuarioMapper.atualizarEntidade(usuario, request);
        usuario.setEmail(email);
        if (request.senha() != null) {
            usuario.setSenha(passwordEncoder.encode(request.senha()));
        }

        return usuarioMapper.paraResposta(usuarioRepository.saveAndFlush(usuario));
    }

    @Transactional
    public void deletar(Long id) {
        usuarioRepository.delete(obterOuLancarExcecao(id));
    }

    private Usuario obterOuLancarExcecao(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id));
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}