package br.com.voupassar.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Troca de senha autenticada (TASK 3.5). Invalida todos os tokens vigentes
 * (bump de {@code credential_version} + revogação de refreshes) e devolve
 * um par novo — o cliente segue logado sem pedir login de novo.
 */
public record ChangePasswordRequest(
    @Schema(example = "senha-segura-123")
        @NotBlank(message = "Senha atual é obrigatória.")
        String currentPassword,
    @Schema(example = "nova-senha-456")
        @NotBlank(message = "Nova senha é obrigatória.")
        @Size(min = 8, max = 72, message = "Senha deve ter de 8 a 72 caracteres.")
        String newPassword) {}
