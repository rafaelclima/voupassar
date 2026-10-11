package br.com.voupassar.aiprova.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Resposta a uma posição do caderno-IA (TASK A.3).
 *
 * <p>Escreve em {@code ai_generation_answers}, nunca em {@code
 * question_attempts} (isolamento). Posição fora de 1..N → 404; execução
 * encerrada → 409.
 */
public record AiAnswerRequest(
    @Schema(example = "1", description = "Posição no caderno-IA (1..N).")
    Integer position,
    @Schema(example = "B", description = "A, B, C, D ou BLANK (em branco conta como erro).")
    String selectedOption,
    @Schema(example = "45", nullable = true, description = "Segundos gastos (opcional).")
    Integer timeSpentSeconds) {}
