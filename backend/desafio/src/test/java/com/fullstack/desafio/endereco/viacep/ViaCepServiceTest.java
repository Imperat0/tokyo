package com.fullstack.desafio.endereco.viacep;

import com.fullstack.desafio.exception.InvalidCepException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ViaCepServiceTest {

    private MockRestServiceServer server;
    private ViaCepService viaCepService;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://viacep.test/ws");
        server = MockRestServiceServer.bindTo(builder).build();
        viaCepService = new ViaCepService(builder.build());
    }

    @Test
    void buscaCepEDevolveEndereco() {
        server.expect(requestTo("https://viacep.test/ws/01001000/json/"))
                .andRespond(withSuccess("""
                        {
                          "cep": "01001-000",
                          "logradouro": "Praça da Sé",
                          "bairro": "Sé",
                          "localidade": "São Paulo",
                          "uf": "SP"
                        }
                        """, MediaType.APPLICATION_JSON));

        ViaCepResponse response = viaCepService.buscar("01001-000");

        assertEquals("Praça da Sé", response.logradouro());
        assertEquals("Sé", response.bairro());
        assertEquals("São Paulo", response.cidade());
        assertEquals("SP", response.estado());
        server.verify();
    }

    @Test
    void rejeitaCepNaoEncontradoPeloServico() {
        server.expect(requestTo("https://viacep.test/ws/00000000/json/"))
                .andRespond(withSuccess("""
                        {"erro": true}
                        """, MediaType.APPLICATION_JSON));

        assertThrows(InvalidCepException.class, () -> viaCepService.buscar("00000-000"));
        server.verify();
    }

    @Test
    void rejeitaCepComFormatoInvalidoSemConsultarServico() {
        assertThrows(InvalidCepException.class, () -> viaCepService.buscar("123"));
        server.verify();
    }
}
