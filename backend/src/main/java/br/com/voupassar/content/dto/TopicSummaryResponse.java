package br.com.voupassar.content.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Linha da listagem {@code GET /api/v1/topics}. */
@Schema(description = "Assunto da taxonomia v1.1 com contagem histórica.")
public record TopicSummaryResponse(
    @Schema(example = "1") long id,
    @Schema(example = "ALGEBRA") String code,
    @Schema(example = "Álgebra") String name,
    @Schema(example = "MATEMATICA") String disciplineCode,
    @Schema(example = "Matemática") String disciplineName,
    @Schema(description = "false = desativado (preserva histórico, nunca apaga).")
        boolean active,
    @Schema(description = "Questões classificadas neste assunto (não-rejeitadas).", example = "18")
        long questionCount,
    @Schema(example = "4") long subtopicCount) {}
