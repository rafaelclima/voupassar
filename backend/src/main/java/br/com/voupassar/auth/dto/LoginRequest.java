package br.com.voupassar.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Login (TASK 3.5). Falha sempre genérica (anti-enumeração). */
public record LoginRequest(
    @Schema(example = "estudante@exemplo.com")
        @NotBlank(message = "E-mail é obrigatório.")
        @Email(message = "E-mail inválido.")
        String email,
    @Schema(example = "senha-segura-123")
        @NotBlank(message = "Senha é obrigatória.")
        String password) {}
