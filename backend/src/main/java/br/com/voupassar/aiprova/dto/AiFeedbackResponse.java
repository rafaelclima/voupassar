package br.com.voupassar.aiprova.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Feedback imediato de uma posição-IA (TASK A.3).
 *
 * <p>Disponível no Modo ESTUDO em qualquer status e no Modo PROVA somente
 * após encerrar (durante a prova → 409); sem resposta → 409. Vale a
 * <b>última</b> resposta, confrontada com o gabarito-IA congelado. Sem
 * assunto classificado por curadoria (questão-IA não entra no mapa de
 * conteúdo): sai o {@code topic} rotulado na geração + selo experimental.
 */
public record AiFeedbackResponse(
    @Schema(example = "55") long attemptId,
    @Schema(example = "1") int position,
    @Schema(example = "MATEMATICA") String discipline,
    @Schema(example = "ALGEBRA", description = "Rótulo de assunto da geração (não é classificação curada).")
    String topic,
    @Schema(example = "B", description = "Última resposta vinculada a esta execução.")
    String selectedOption,
    @Schema(example = "true") Boolean isCorrect,
    @Schema(example = "B", description = "Resposta do gabarito-IA congelado (sem garantia pedagógica).")
    String correctAnswer,
    @Schema(example = "true", description = "Selo experimental: questão gerada por IA, não oficial.")
    boolean aiGenerated,
    List<String> notes) {}
