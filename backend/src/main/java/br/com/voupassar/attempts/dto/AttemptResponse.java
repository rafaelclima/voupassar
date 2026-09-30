package br.com.voupassar.attempts.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Tentativa registrada (TASK 3.7) — fato imutável com correção do servidor.
 *
 * <p>{@code isCorrect} é NULL quando {@code wasAnnulled} (pontuação de
 * anuladas DESCONHECIDA, TASK 1.3 §4). O resultado é sempre devolvido pela
 * API (decisão da TASK 3.7): ocultar o gabarito durante o Modo Prova é
 * responsabilidade da camada de simulados (Fase 5), nunca desta escrita.
 */
public record AttemptResponse(
    @Schema(example = "101") long id,
    @Schema(example = "12") long questionId,
    @Schema(example = "C") String selectedOption,
    @Schema(example = "true", nullable = true) Boolean isCorrect,
    @Schema(example = "false") boolean wasAnnulled,
    @Schema(example = "ESTUDO") String mode,
    @Schema(example = "95", nullable = true) Integer timeSpentSeconds,
    @Schema(nullable = true) Long studySessionId,
    @Schema(nullable = true) Long simulationAttemptId,
    OffsetDateTime answeredAt,
    List<String> notes) {}
