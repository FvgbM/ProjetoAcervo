package com.backend.acervo.dto;

public record RequisicaoAlterarSenha(
    String email,
    String senhaAtual,
    String novaSenha
) {}