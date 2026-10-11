package br.com.voupassar.aiprova.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Confirmação de resposta-IA (TASK A.3).
 *
 * <p>No Modo ESTUDO revela acerto/erro imediatamente; no Modo PROVA em
 * andamento o resultado fica oculto ( {@code isCorrect} e {@code
 * frozenAnswerKey} nulos) até encerrar — mesmo contrato do oficial.
 */
public record AiAnswerResponse(
    @Schema(example = "9") long answerId,
    @Schema(example = "55") long attemptId,
    @Schema(example = "1") int position,
    @Schema(example = "B") String selectedOption,
    @Schema(nullable = true, description = "NULL no Modo Prova em andamento (gabarito oculto).")
    Boolean isCorrect,
    @Schema(nullable = true, description = "Gabarito-IA congelado (só fora de prova em andamento).")
    String frozenAnswerKey,
    @Schema(example = "true", description = "Selo experimental: questão gerada por IA, não oficial.")
    boolean aiGenerated,
    List<String> notes) {}
