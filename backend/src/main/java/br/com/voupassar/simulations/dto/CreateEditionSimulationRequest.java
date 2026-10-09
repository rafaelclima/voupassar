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
    @Schema(example = "2026", description = "Ano da edição real (IFRN-2021 ausente; EAJ: só 2021/2022/2025).")
        @NotNull(message = "Ano da edição é obrigatório.")
        @Min(value = 2000, message = "Ano da edição inválido.")
        @Max(value = 2100, message = "Ano da edição inválido.")
        Integer editionYear,
    @Schema(example = "PROVA")
        @NotBlank(message = "Modo é obrigatório.")
        @Pattern(
            regexp = "(?i)ESTUDO|PROVA",
            message = "Modo deve ser ESTUDO ou PROVA.")
        String mode,
    @Schema(
            example = "IFRN",
            description = "Processo seletivo: IFRN ou EAJ (TASK E.1). Ausente = IFRN (compatibilidade); ano sozinho nunca decide (EAJ-2022 ≠ IFRN-2022).")
        @Pattern(
            regexp = "(?i)IFRN|EAJ",
            message = "Processo seletivo inválido (permitido IFRN, EAJ).")
        String institution) {

  /** Compatibilidade: corpo antigo sem {@code institution} = IFRN. */
  public CreateEditionSimulationRequest(Integer editionYear, String mode) {
    this(editionYear, mode, null);
  }
}
