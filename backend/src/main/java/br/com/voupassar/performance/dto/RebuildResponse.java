package br.com.voupassar.performance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

/**
 * Resultado do rebuild do agregado {@code student_topic_performance}
 * (TASK 4.1) — delete + insert idempotente a partir do fato imutável.
 */
public record RebuildResponse(
    @Schema(example = "7") int rebuiltRows,
    OffsetDateTime rebuiltAt) {}
