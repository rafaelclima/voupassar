package br.com.voupassar.exams.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Linha da listagem {@code GET /api/v1/editions}. */
@Schema(description = "Resumo de uma edição presente no repositório.")
public record EditionSummaryResponse(
    @Schema(example = "2026") int year,
    @Schema(description = "Processo seletivo: IFRN ou EAJ (TASK C.1).", example = "IFRN")
        String institution,
    @Schema(example = "48/2025") String edital,
    @Schema(example = "240") int durationMinutes,
    @Schema(example = "40") int objectiveCount,
    @Schema(example = "20") int lpCount,
    @Schema(example = "20") int matCount,
    @Schema(description = "Contagem de Ciências da Natureza daquela edição (V17; C.2 semeia CN/CH).")
        int cnCount,
    @Schema(description = "Contagem de Ciências Humanas daquela edição (V17; C.2 semeia CN/CH).")
        int chCount,
    @Schema(example = "true") boolean hasEssay,
    @Schema(description = "NULL = regra de pontuação DESCONHECIDA (TASK 1.3 §4).", nullable = true)
        String scoringRule,
    @Schema(example = "1") long versionCount,
    @Schema(example = "2") long documentCount) {}
