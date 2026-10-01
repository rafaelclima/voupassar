package br.com.voupassar.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Resultado da sessão de revisão (TASK 5.4) — correção completa após encerrar.
 *
 * <p>Regras de evidência:
 * <ul>
 *   <li>Vale a <b>última</b> tentativa com {@code mode=REVISAO} por questão
 *       vinculada a esta sessão (o fato é imutável — correção só via nova
 *       tentativa, TASK 3.7); questões sem resposta aparecem como não
 *       respondidas, nunca como erro inventado.</li>
 *   <li>Anuladas ficam fora do aproveitamento (pontuação DESCONHECIDA, TASK
 *       1.3 §4): contam no resumo, nunca como acerto nem como erro.</li>
 *   <li>{@code accuracy} NULL = sem tentativas pontuáveis (nunca zero
 *       inventado). O confronto usa o gabarito vigente via fato persistido
 *       (a revisão não congela gabarito — diferença intencional para o
 *       simulado da TASK 5.1).</li>
 * </ul>
 */
public record ReviewSessionResultResponse(
    @Schema(example = "12") long sessionId,
    @Schema(example = "REVISAO") String mode,
    @Schema(example = "FINISHED") String status,
    @Schema(example = "10") long total,
    @Schema(example = "10") long answered,
    @Schema(example = "0") long unanswered,
    @Schema(example = "10") long scored,
    @Schema(example = "7") long correct,
    @Schema(example = "3") long incorrect,
    @Schema(example = "0") long annulled,
    @Schema(example = "0.7", nullable = true) Double accuracy,
    @Schema(description = "Correção por posição do caderno (ordem das posições).")
    List<ResultItem> items,
    List<String> notes) {

  /** Correção de uma posição: última resposta REVISAO nesta sessão. */
  public record ResultItem(
      @Schema(example = "1") int position,
      @Schema(example = "42") long questionId,
      @Schema(example = "MATEMATICA") String disciplineCode,
      @Schema(example = "2026", nullable = true) Integer sourceYear,
      @Schema(example = "17", nullable = true) Integer sourceQuestionNumber,
      @Schema(nullable = true, description = "Última resposta (NULL = não respondida).")
      String selectedOption,
      @Schema(nullable = true, description = "NULL quando anulada ou não respondida.")
      Boolean isCorrect,
      boolean wasAnnulled,
      @Schema(description = "true quando sem tentativa vinculada a esta sessão.")
      boolean unanswered) {}
}
