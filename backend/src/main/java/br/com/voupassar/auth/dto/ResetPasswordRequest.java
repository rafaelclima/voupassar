package br.com.voupassar.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Efetivação da recuperação com token de uso único (TASK 3.5). */
public record ResetPasswordRequest(
    @Schema(description = "Token opaco de recuperação (uso único, expira em 60 min por padrão).")
        @NotBlank(message = "Token é obrigatório.")
        String token,
    @Schema(example = "nova-senha-456")
        @NotBlank(message = "Nova senha é obrigatória.")
        @Size(min = 8, max = 72, message = "Senha deve ter de 8 a 72 caracteres.")
        String newPassword) {}
