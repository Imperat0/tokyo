package com.fullstack.desafio.auth;

public record IssuedToken(String value, long expiresIn) {
}
