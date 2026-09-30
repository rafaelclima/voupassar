package br.com.voupassar.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** Logout de uma sessão: revoga um refresh token (idempotente). */
public record LogoutRequest(
    @Schema(description = "Refresh token opaco a revogar.")
        @NotBlank(message = "Refresh token é obrigatório.")
        String refreshToken) {}
