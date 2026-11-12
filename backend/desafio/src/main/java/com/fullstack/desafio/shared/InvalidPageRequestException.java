package com.fullstack.desafio.shared;

public class InvalidPageRequestException extends RuntimeException {

    public InvalidPageRequestException(String message) {
        super(message);
    }
}
