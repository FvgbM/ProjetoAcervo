package com.backend.acervo.exceptions;

public class TokenInvalidoException extends RuntimeException {
    public TokenInvalidoException(String mensagem) {
        super(mensagem);
    }
}