package br.com.voupassar.simulations.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Criação de simulado real por edição (TASK 5.5).
 *
 * <p>A estrutura (quantidade, divisão por disciplina, discursiva) é lida da
 * configuração da própria edição ({@code exams} + {@code exam_essay_prompts}),
 * nunca de uma regra universal: cada edição possui configuração própria
 * (AGENTS.md §10). O caderno é integral e em ordem original, incluindo as
 * anuladas nas posições originais (fora do aproveitamento).
 */
public record CreateEditionSimulationRequest(
    @Schema(example = "2026", description = "Ano da edição real (2021 ausente do dataset).")
        @NotNull(message = "Ano da edição é obrigatório.")
        @Min(value = 2000, message = "Ano da edição inválido.")
        @Max(value = 2100, message = "Ano da edição inválido.")
        Integer editionYear,
    @Schema(example = "PROVA")
        @NotBlank(message = "Modo é obrigatório.")
        @Pattern(
            regexp = "(?i)ESTUDO|PROVA",
            message = "Modo deve ser ESTUDO ou PROVA.")
        String mode) {}
