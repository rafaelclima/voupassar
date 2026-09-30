package br.com.voupassar.content.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Agregado por assunto no panorama {@code GET /api/v1/content/stats}. */
@Schema(description = "Frequência histórica de um assunto (derivado das classificações).")
public record ContentTopicStatsResponse(
    @Schema(example = "5") long topicId,
    @Schema(example = "ALGEBRA") String code,
    @Schema(example = "Álgebra") String name,
    @Schema(example = "MATEMATICA") String disciplineCode,
    @Schema(example = "Matemática") String disciplineName,
    @Schema(example = "18") long questions,
    @Schema(description = "Percentual sobre o total classificado.", example = "7.5")
        double percentOfClassified,
    @Schema(description = "Nº de edições em que aparece.", example = "6") int editionsCount,
    @Schema(description = "Anuladas — contam como conteúdo, sem pontuar.", example = "0")
        long annulled) {}
