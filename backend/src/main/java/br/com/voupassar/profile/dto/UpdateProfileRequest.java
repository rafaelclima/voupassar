package br.com.voupassar.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Atualização do perfil (TASK 3.6).
 *
 * <p>Mesmas regras do registro (TASK 3.5): nome obrigatório 2–80; opcionais
 * aceitam NULL (ausente = manter/apagar conforme enviado; em-branco vira
 * NULL). E-mail e senha não mudam aqui (senha em
 * {@code /auth/password/change}).
 */
public record UpdateProfileRequest(
    @Schema(example = "Maria da Silva")
        @NotBlank(message = "Nome de exibição é obrigatório.")
        @Size(min = 2, max = 80, message = "Nome deve ter de 2 a 80 caracteres.")
        String displayName,
    @Schema(example = "9º ano", nullable = true)
        @Size(max = 40, message = "Ano escolar deve ter até 40 caracteres.")
        String schoolYear,
    @Schema(example = "2027", nullable = true)
        @Min(value = 2000, message = "Ano-alvo inválido.")
        @Max(value = 2100, message = "Ano-alvo inválido.")
        Integer targetYear,
    @Schema(nullable = true)
        @Size(max = 500, message = "Objetivo deve ter até 500 caracteres.")
        String studyGoal) {}
