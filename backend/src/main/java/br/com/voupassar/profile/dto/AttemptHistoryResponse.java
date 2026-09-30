package br.com.voupassar.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

/**
 * Item do histórico de tentativas (TASK 3.6 — leitura).
 *
 * <p>Proveniência mínima da questão respondida (ano-fonte, número,
 * disciplina) sem expor enunciado/gabarito aqui (isso é a TASK 3.4).
 * {@code isCorrect} NULL quando anulada (sem pontuar — regra DESCONHECIDA).
 */
public record AttemptHistoryResponse(
    @Schema(example = "101") long id,
    @Schema(example = "12") long questionId,
    @Schema(example = "2026", nullable = true) Integer sourceYear,
    @Schema(example = "17", nullable = true) Integer sourceNumber,
    @Schema(example = "MATEMATICA") String disciplineCode,
    @Schema(example = "Matemática") String disciplineName,
    @Schema(example = "C") String selectedOption,
    @Schema(example = "true", nullable = true) Boolean isCorrect,
    @Schema(example = "false") boolean wasAnnulled,
    @Schema(example = "ESTUDO") String mode,
    @Schema(example = "95", nullable = true) Integer timeSpentSeconds,
    @Schema(nullable = true) Long studySessionId,
    @Schema(nullable = true) Long simulationAttemptId,
    OffsetDateTime answeredAt) {}
