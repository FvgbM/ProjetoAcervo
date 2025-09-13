package com.backend.acervo.exceptions;

public class UsuarioNaoEncontradoException extends RuntimeException {
    public UsuarioNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}