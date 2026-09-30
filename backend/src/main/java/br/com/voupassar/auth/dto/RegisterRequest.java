package br.com.voupassar.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cadastro (TASK 3.5). Cria usuário + papel STUDENT + perfil mínimo. */
public record RegisterRequest(
    @Schema(example = "estudante@exemplo.com")
        @NotBlank(message = "E-mail é obrigatório.")
        @Email(message = "E-mail inválido.")
        @Size(max = 254, message = "E-mail deve ter até 254 caracteres.")
        String email,
    @Schema(example = "senha-segura-123", description = "Mínimo 8, máximo 72 caracteres (limite do bcrypt).")
        @NotBlank(message = "Senha é obrigatória.")
        @Size(min = 8, max = 72, message = "Senha deve ter de 8 a 72 caracteres.")
        String password,
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
