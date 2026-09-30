package br.com.voupassar.content.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** Detalhe {@code GET /api/v1/topics/{id}} com série histórica por edição. */
@Schema(description = "Assunto com subassuntos e distribuição histórica por edição.")
public record TopicDetailResponse(
    @Schema(example = "5") long id,
    @Schema(example = "ALGEBRA") String code,
    @Schema(example = "Álgebra") String name,
    @Schema(example = "MATEMATICA") String disciplineCode,
    @Schema(example = "Matemática") String disciplineName,
    boolean active,
    @Schema(description = "Questões classificadas neste assunto (não-rejeitadas).", example = "18")
        long questionCount,
    @Schema(description = "Edições em que o assunto aparece.", example = "[2020, 2022]")
        List<Integer> editions,
    List<EditionCountResponse> perEdition,
    ConfidenceBreakdownResponse confidence,
    List<SubtopicSummaryResponse> subtopics) {}
