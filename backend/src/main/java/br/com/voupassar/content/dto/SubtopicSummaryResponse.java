package br.com.voupassar.content.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Linha da listagem {@code GET /api/v1/subtopics}. */
@Schema(description = "Subassunto da taxonomia v1.1 com contagem histórica.")
public record SubtopicSummaryResponse(
    @Schema(example = "25") long id,
    @Schema(example = "EQUACOES") String code,
    @Schema(example = "Equações") String name,
    @Schema(example = "5") long topicId,
    @Schema(example = "ALGEBRA") String topicCode,
    @Schema(example = "Álgebra") String topicName,
    @Schema(example = "MATEMATICA") String disciplineCode,
    @Schema(example = "Matemática") String disciplineName,
    boolean active,
    @Schema(description = "Questões classificadas neste subassunto (não-rejeitadas).", example = "8")
        long questionCount) {}
