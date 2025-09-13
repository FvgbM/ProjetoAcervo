package com.backend.acervo.exceptions;

public class TokenExpiradoException extends RuntimeException {
    public TokenExpiradoException(String mensagem) {
        super(mensagem);
    }
}