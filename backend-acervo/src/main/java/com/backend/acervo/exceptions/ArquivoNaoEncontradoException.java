package com.backend.acervo.exceptions;

public class ArquivoNaoEncontradoException extends RuntimeException {
    public ArquivoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
