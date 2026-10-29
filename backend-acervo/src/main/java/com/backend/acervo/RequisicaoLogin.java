package com.backend.acervo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RequisicaoLogin(
        @NotBlank @Email String email,
        @NotBlank String senha
) { }