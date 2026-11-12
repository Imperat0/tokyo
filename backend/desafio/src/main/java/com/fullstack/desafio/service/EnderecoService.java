package com.fullstack.desafio.service;

import com.fullstack.desafio.endereco.Endereco;
import com.fullstack.desafio.endereco.EnderecoMapper;
import com.fullstack.desafio.endereco.EnderecoRepository;
import com.fullstack.desafio.endereco.dto.EnderecoRequest;
import com.fullstack.desafio.endereco.dto.EnderecoResponse;
import com.fullstack.desafio.endereco.viacep.ViaCepService;
import com.fullstack.desafio.exception.ResourceNotFoundException;
import com.fullstack.desafio.shared.PageResponse;
import com.fullstack.desafio.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnderecoService {

    private final EnderecoRepository enderecoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EnderecoMapper enderecoMapper;
    private final ViaCepService viaCepService;

    @Transactional
    public EnderecoResponse criar(Long usuarioId, EnderecoRequest request) {
        validarExistenciaUsuario(usuarioId);

        Endereco endereco = enderecoMapper.paraEntidade(request, viaCepService.buscar(request.cep()));
        endereco.setUsuario(usuarioRepository.getReferenceById(usuarioId));

        return enderecoMapper.paraResposta(enderecoRepository.save(endereco));
    }

    public PageResponse<EnderecoResponse> buscarPorUsuario(Long usuarioId, Pageable pageable) {
        validarExistenciaUsuario(usuarioId);
        return PageResponse.from(enderecoRepository.findByUsuarioId(usuarioId, pageable).map(enderecoMapper::paraResposta));
    }

    public PageResponse<EnderecoResponse> buscarTodos(Pageable pageable) {
        return PageResponse.from(enderecoRepository.findAll(pageable).map(enderecoMapper::paraResposta));
    }

    public EnderecoResponse buscarPorId(Long id) {
        return enderecoMapper.paraResposta(obterOuLancarExcecao(id));
    }

    @Transactional
    public EnderecoResponse atualizar(Long id, EnderecoRequest request) {
        Endereco endereco = obterOuLancarExcecao(id);
        enderecoMapper.atualizarEntidade(endereco, request, viaCepService.buscar(request.cep()));
        return enderecoMapper.paraResposta(enderecoRepository.saveAndFlush(endereco));
    }

    @Transactional
    public void deletar(Long id) {
        enderecoRepository.delete(obterOuLancarExcecao(id));
    }

    private Endereco obterOuLancarExcecao(Long id) {
        return enderecoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço", id));
    }

    private void validarExistenciaUsuario(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("Usuário", usuarioId);
        }
    }
}
