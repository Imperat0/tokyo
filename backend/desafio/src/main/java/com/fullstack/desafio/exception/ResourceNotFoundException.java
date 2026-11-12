package com.fullstack.desafio.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " não encontrado(a) com id " + id);
    }
}
