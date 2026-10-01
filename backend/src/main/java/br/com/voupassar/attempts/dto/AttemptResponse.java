package br.com.voupassar.attempts.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Tentativa registrada (TASK 3.7) — fato imutável com correção do servidor,
 * mais ocultação do Modo Prova (TASK 5.3).
 *
 * <p>{@code isCorrect} é NULL quando {@code wasAnnulled} (pontuação de
 * anuladas DESCONHECIDA, TASK 1.3 §4) e também quando a tentativa é {@code
 * PROVA} com execução/sessão ainda {@code IN_PROGRESS} (resultado oculto
 * até encerrar — AGENTS.md §9; o valor correto segue persistido e reaparece
 * após concluir/abandonar/encerrar).
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
