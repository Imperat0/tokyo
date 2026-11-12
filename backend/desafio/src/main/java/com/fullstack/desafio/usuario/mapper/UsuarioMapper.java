package com.fullstack.desafio.usuario.mapper;

import com.fullstack.desafio.usuario.Usuario;
import com.fullstack.desafio.usuario.Perfil;
import com.fullstack.desafio.usuario.dto.UsuarioAtualizarRequest;
import com.fullstack.desafio.usuario.dto.UsuarioCriarRequest;
import com.fullstack.desafio.usuario.dto.UsuarioResponse;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public Usuario paraEntidade(UsuarioCriarRequest request) {
        Usuario usuario = new Usuario();
        usuario.setNome(request.nome().trim());
        usuario.setPerfil(Perfil.USER);
        return usuario;
    }

    public void atualizarEntidade(Usuario usuario, UsuarioAtualizarRequest request) {
        usuario.setNome(request.nome().trim());
    }

    public UsuarioResponse paraResposta(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil(),
                usuario.getCreatedAt(),
                usuario.getUpdatedAt()
        );
    }
}
