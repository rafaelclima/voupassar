package br.com.voupassar.simulations.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

/**
 * Linha da lista de execuções do aluno (TASK 5.1).
 *
 * <p>Sem nota aqui: o desempenho detalhado sai de {@code GET
 * /attempts/{id}/result} (somente após encerrar). Disciplina e tamanho vêm do
 * caderno congelado — nunca do {@code filter_json} cru.
 */
public record SimulationAttemptSummary(
    @Schema(example = "55") long attemptId,
    @Schema(example = "7") long simulationId,
    @Schema(example = "Simulado Matemática — 10 questões [PROVA]") String title,
    @Schema(example = "MATEMATICA") String disciplineCode,
    @Schema(example = "PROVA") String mode,
    @Schema(example = "SUBMITTED") String status,
    @Schema(example = "10") int questionCount,
    OffsetDateTime startedAt,
    @Schema(nullable = true) OffsetDateTime submittedAt) {}
