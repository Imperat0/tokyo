package com.fullstack.desafio.exception;

public class InvalidCepException extends RuntimeException {

    public InvalidCepException() {
        super("CEP não encontrado. Verifique o número informado.");
    }
}
