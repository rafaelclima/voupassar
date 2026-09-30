package br.com.voupassar.content.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** Detalhe {@code GET /api/v1/disciplines/{code}} com os assuntos da disciplina. */
@Schema(description = "Disciplina com seus assuntos (taxonomia v1.1).")
public record DisciplineDetailResponse(
    @Schema(example = "MATEMATICA") String code,
    @Schema(example = "Matemática") String name,
    @Schema(example = "8") long topicCount,
    @Schema(description = "Questões importadas nesta disciplina.", example = "120")
        long questionCount,
    List<TopicSummaryResponse> topics) {}
