package br.com.voupassar.content.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Linha da listagem {@code GET /api/v1/disciplines}. */
@Schema(description = "Resumo de uma disciplina observada nas provas.")
public record DisciplineSummaryResponse(
    @Schema(example = "LINGUA_PORTUGUESA") String code,
    @Schema(example = "Língua Portuguesa") String name,
    @Schema(description = "Assuntos cadastrados (taxonomia v1.1, seed V2).", example = "2")
        long topicCount,
    @Schema(description = "Questões importadas (TASK 2.3) nesta disciplina.", example = "120")
        long questionCount) {}
