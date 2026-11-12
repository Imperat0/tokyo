package com.fullstack.desafio.endereco;

import com.fullstack.desafio.endereco.dto.EnderecoRequest;
import com.fullstack.desafio.endereco.dto.EnderecoResponse;
import com.fullstack.desafio.endereco.viacep.ViaCepResponse;
import org.springframework.stereotype.Component;

@Component
public class EnderecoMapper {

    public Endereco paraEntidade(EnderecoRequest request, ViaCepResponse viaCep) {
        Endereco endereco = new Endereco();
        atualizarEntidade(endereco, request, viaCep);
        return endereco;
    }

    public void atualizarEntidade(Endereco endereco, EnderecoRequest request, ViaCepResponse viaCep) {
        endereco.setRua(viaCep.logradouro().trim());
        endereco.setNumero(request.numero().trim());
        endereco.setComplemento(vazioParaNulo(request.complemento()));
        endereco.setBairro(viaCep.bairro().trim());
        endereco.setCidade(viaCep.cidade().trim());
        endereco.setEstado(viaCep.estado().trim().toUpperCase(java.util.Locale.ROOT));
        endereco.setCep(request.cep().replaceAll("\\D", ""));
    }

    public EnderecoResponse paraResposta(Endereco endereco) {
        return new EnderecoResponse(
                endereco.getId(),
                endereco.getRua(),
                endereco.getNumero(),
                endereco.getComplemento(),
                endereco.getBairro(),
                endereco.getCidade(),
                endereco.getEstado(),
                endereco.getCep(),
                endereco.getUsuario().getId(),
                endereco.getCreatedAt(),
                endereco.getUpdatedAt()
        );
    }

    private String vazioParaNulo(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}