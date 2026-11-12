package com.fullstack.desafio.endereco.viacep;

import com.fullstack.desafio.exception.InvalidCepException;
import com.fullstack.desafio.exception.ViaCepUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
public class ViaCepService {

    @Qualifier("viaCepRestClient")
    private final RestClient restClient;

    public ViaCepResponse buscar(String cepInformado) {
        String cep = cepInformado == null ? "" : cepInformado.replaceAll("\\D", "");
        if (!cep.matches("\\d{8}")) {
            throw new InvalidCepException();
        }

        ViaCepResponse response;
        try {
            response = restClient.get()
                    .uri("/{cep}/json/", cep)
                    .retrieve()
                    .body(ViaCepResponse.class);
        } catch (RestClientException ex) {
            throw new ViaCepUnavailableException(ex);
        }

        if (response == null) {
            throw new ViaCepUnavailableException();
        }
        if (Boolean.TRUE.equals(response.erro())) {
            throw new InvalidCepException();
        }

        String cepRetornado = response.cep() == null ? "" : response.cep().replaceAll("\\D", "");
        if (!cep.equals(cepRetornado)
                || response.logradouro() == null
                || response.bairro() == null
                || response.cidade() == null
                || response.estado() == null) {
            throw new ViaCepUnavailableException();
        }
        return response;
    }
}
