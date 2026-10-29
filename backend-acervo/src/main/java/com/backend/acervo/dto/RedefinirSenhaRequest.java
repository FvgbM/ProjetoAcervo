package com.backend.acervo.dto;

import jakarta.validation.constraints.NotBlank;

public record RedefinirSenhaRequest(
        @NotBlank String token,
        @NotBlank String novaSenha
) { }