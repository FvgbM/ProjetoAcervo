package com.backend.acervo.exceptions;

public class UsuarioInativoException extends RuntimeException {
    public UsuarioInativoException(String mensagem) {
        super(mensagem);
    }
}