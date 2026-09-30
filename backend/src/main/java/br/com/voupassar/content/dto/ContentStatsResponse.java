package br.com.voupassar.content.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Panorama histórico {@code GET /api/v1/content/stats}.
 *
 * <p>Frequências derivadas das classificações v1.1 no banco (revisão humana
 * PENDENTE) — nunca verdade oficial do IFRN. Tendência NÃO é calculada aqui
 * (6 edições, sem teste estatístico; oscilações de 1–2 questões são ruído —
 * ver {@code docs/content-map.md}).
 */
@Schema(description = "Panorama histórico dos conteúdos (derivado do banco, revisão pendente).")
public record ContentStatsResponse(
    @Schema(description = "Questões oficiais importadas (TASK 2.3).", example = "240")
        long totalQuestions,
    @Schema(description = "Com classificação não-rejeitada.", example = "240")
        long totalClassified,
    @Schema(description = "Versões de taxonomia presentes no banco.", example = "[\"v1.1\"]")
        List<String> taxonomyVersions,
    @Schema(
            description = "true = há classificações aguardando curadoria (TASK 12.2).",
            example = "true")
        boolean classificationReviewPending,
    List<ContentDisciplineStatsResponse> perDiscipline,
    List<ContentTopicStatsResponse> perTopic,
    List<String> notes) {}
