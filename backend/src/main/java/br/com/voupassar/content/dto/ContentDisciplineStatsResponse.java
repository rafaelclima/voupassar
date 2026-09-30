package br.com.voupassar.content.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Agregado por disciplina no panorama {@code GET /api/v1/content/stats}. */
@Schema(description = "Panorama histórico por disciplina.")
public record ContentDisciplineStatsResponse(
    @Schema(example = "MATEMATICA") String disciplineCode,
    @Schema(example = "Matemática") String disciplineName,
    @Schema(example = "8") long topics,
    @Schema(description = "Questões importadas (discipline_id factual).", example = "120")
        long questions) {}
