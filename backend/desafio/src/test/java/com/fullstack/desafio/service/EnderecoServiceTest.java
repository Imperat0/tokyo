package com.fullstack.desafio.service;

import com.fullstack.desafio.endereco.Endereco;
import com.fullstack.desafio.endereco.EnderecoMapper;
import com.fullstack.desafio.endereco.EnderecoRepository;
import com.fullstack.desafio.endereco.dto.EnderecoRequest;
import com.fullstack.desafio.endereco.viacep.ViaCepResponse;
import com.fullstack.desafio.endereco.viacep.ViaCepService;
import com.fullstack.desafio.exception.ResourceNotFoundException;
import com.fullstack.desafio.usuario.Usuario;
import com.fullstack.desafio.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnderecoServiceTest {

    @Mock
    private EnderecoRepository enderecoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ViaCepService viaCepService;

    private EnderecoService enderecoService;

    @BeforeEach
    void setUp() {
        enderecoService = new EnderecoService(
                enderecoRepository, usuarioRepository, new EnderecoMapper(), viaCepService);
    }

    @Test
    void criaEnderecoComDadosDeLogradouroObtidosDoViaCep() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        when(usuarioRepository.getReferenceById(1L)).thenReturn(new Usuario());
        when(viaCepService.buscar("01001-000")).thenReturn(
                new ViaCepResponse("01001-000", "Praça da Sé", "Sé", "São Paulo", "SP", null));
        when(enderecoRepository.save(any(Endereco.class))).thenAnswer(invocation -> invocation.getArgument(0));

        enderecoService.criar(1L, new EnderecoRequest(
                null, "10", null, null, null, null, "01001-000"));

        var captor = org.mockito.ArgumentCaptor.forClass(Endereco.class);
        verify(enderecoRepository).save(captor.capture());
        assertEquals("Praça da Sé", captor.getValue().getRua());
        assertEquals("Sé", captor.getValue().getBairro());
        assertEquals("São Paulo", captor.getValue().getCidade());
        assertEquals("SP", captor.getValue().getEstado());
        assertEquals("10", captor.getValue().getNumero());
    }

    @Test
    void naoConsultaViaCepQuandoUsuarioNaoExiste() {
        when(usuarioRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> enderecoService.criar(
                99L, new EnderecoRequest(null, "10", null, null, null, null, "01001-000")));
    }

    @Test
    void retornaErroAoAtualizarEnderecoInexistente() {
        when(enderecoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> enderecoService.atualizar(
                99L, new EnderecoRequest(null, "10", null, null, null, null, "01001-000")));
    }
}
