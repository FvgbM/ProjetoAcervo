package com.backend.acervo.exceptions;

public class SenhaIncorretaException extends RuntimeException {
    public SenhaIncorretaException(String mensagem) {
        super(mensagem);
    }
}