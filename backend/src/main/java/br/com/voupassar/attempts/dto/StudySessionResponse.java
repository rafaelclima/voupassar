package br.com.voupassar.attempts.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

/**
 * Sessão de estudo (TASK 3.7) — cabeçalho do bloco que agrupa tentativas.
 */
public record StudySessionResponse(
    @Schema(example = "7") long id,
    @Schema(example = "ESTUDO") String mode,
    @Schema(example = "IN_PROGRESS") String status,
    OffsetDateTime startedAt,
    @Schema(nullable = true) OffsetDateTime finishedAt) {}
