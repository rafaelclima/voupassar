package br.com.voupassar.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** Rotação do refresh token opaco (TASK 3.5). */
public record RefreshRequest(
    @Schema(description = "Refresh token opaco recebido no login/refresh anterior.")
        @NotBlank(message = "Refresh token é obrigatório.")
        String refreshToken) {}
