package br.com.voupassar.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Classificação revisada (TASK 12.1) — recibo da curadoria com o revisor.
 *
 * <p>{@code reviewedAt} é carimbado no banco ({@code now()}); a leitura do
 * instante fica para a fila/métricas — aqui o essencial é quem e o quê.
 */
public record ClassificationReviewResponse(
    @Schema(example = "10") long id,
    @Schema(example = "1") long questionId,
    @Schema(example = "APPROVED") String status,
    @Schema(example = "Assunto confirmado contra o caderno, pág. 12.", nullable = true)
        String observation,
    @Schema(example = "7") long reviewedBy) {}
