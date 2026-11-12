package com.fullstack.desafio.endereco.viacep;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ViaCepResponse(
        String cep,
        String logradouro,
        String bairro,
        @JsonProperty("localidade") String cidade,
        @JsonProperty("uf") String estado,
        Boolean erro
) {
}
