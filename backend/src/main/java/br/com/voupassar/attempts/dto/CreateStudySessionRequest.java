package br.com.voupassar.attempts.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Abertura de sessão de estudo (TASK 3.7).
 *
 * <p>Modo segue o {@code CHECK} da DDL V1 ({@code ESTUDO/PROVA/REVISAO}) —
 * maiúsculas; minúsculas são normalizadas pelo serviço antes de persistir.
 */
public record CreateStudySessionRequest(
    @Schema(example = "ESTUDO")
        @NotBlank(message = "Modo é obrigatório.")
        @Pattern(
            regexp = "(?i)ESTUDO|PROVA|REVISAO",
            message = "Modo deve ser ESTUDO, PROVA ou REVISAO.")
        String mode) {}
