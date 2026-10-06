package br.com.voupassar.content.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Panorama histórico {@code GET /api/v1/content/stats}.
 *
 * <p>Frequências derivadas das classificações v1.1 no banco — nunca verdade
 * oficial do IFRN. Confiança BAIXA = assunto NÃO CONFIRMADO. Tendência NÃO é
 * calculada aqui (6 edições, sem teste estatístico; oscilações de 1–2
 * questões são ruído — ver {@code docs/content-map.md}).
 */
@Schema(description = "Panorama histórico dos conteúdos (derivado do banco).")
public record ContentStatsResponse(
    @Schema(description = "Questões oficiais importadas (TASK 2.3).", example = "240")
        long totalQuestions,
    @Schema(description = "Com classificação vigente.", example = "240")
        long totalClassified,
    @Schema(description = "Versões de taxonomia presentes no banco.", example = "[\"v1.1\"]")
        List<String> taxonomyVersions,
    List<ContentDisciplineStatsResponse> perDiscipline,
    List<ContentTopicStatsResponse> perTopic,
    List<String> notes) {}
