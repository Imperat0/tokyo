package com.fullstack.desafio.exception;

public class ViaCepUnavailableException extends RuntimeException {

    public ViaCepUnavailableException() {
        super("Não foi possível validar o CEP no ViaCEP. Tente novamente mais tarde.");
    }

    public ViaCepUnavailableException(Throwable cause) {
        super("Não foi possível validar o CEP no ViaCEP. Tente novamente mais tarde.", cause);
    }
}
