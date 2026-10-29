package com.backend.acervo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SolicitarRecuperacaoRequest(
        @NotBlank @Email String email
) { }