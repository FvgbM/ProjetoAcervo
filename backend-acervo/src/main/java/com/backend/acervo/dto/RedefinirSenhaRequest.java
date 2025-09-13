package com.backend.acervo.dto;

public record RedefinirSenhaRequest(
    String token,
    String novaSenha
) {}