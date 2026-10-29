package com.backend.acervo.dto;

import jakarta.validation.constraints.NotBlank;

public record RequisicaoAlterarSenha(
        @NotBlank String senhaAtual,
        @NotBlank String novaSenha
) { }