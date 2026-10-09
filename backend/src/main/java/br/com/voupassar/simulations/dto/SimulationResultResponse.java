package br.com.voupassar.simulations.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Resultado de simulado por disciplina (TASK 5.1) — correção completa após
 * concluir ou abandonar.
 *
 * <p>Regras de evidência:
 * <ul>
 *   <li>Vale a <b>última</b> tentativa por questão vinculada a esta execução
 *       (o fato é imutável — correção só via nova tentativa, TASK 3.7);
 *       questões sem resposta aparecem como não respondidas, nunca como erro
 *       inventado.</li>
 *   <li>Anuladas ficam fora do aproveitamento (pontuação DESCONHECIDA, TASK
 *       1.3 §4): contam no resumo, nunca como acerto nem como erro.</li>
 *   <li>{@code accuracy} NULL = sem tentativas pontuáveis (nunca zero
 *       inventado). O confronto é contra o gabarito <b>congelado</b> ({@code
 *       frozen_answer_key}), nunca contra o gabarito "atual".</li>
 * </ul>
 */
public record SimulationResultResponse(
    @Schema(example = "55") long attemptId,
    @Schema(example = "7") long simulationId,
    @Schema(example = "Simulado Matemática — 10 questões [PROVA]") String title,
    @Schema(example = "MATEMATICA") String disciplineCode,
    @Schema(example = "PROVA") String mode,
    @Schema(example = "SUBMITTED") String status,
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

  /** Correção de uma posição: última resposta × gabarito congelado. */
  public record ResultItem(
      @Schema(example = "1") int position,
      @Schema(example = "12") long questionId,
      @Schema(example = "MATEMATICA") String disciplineCode,
      @Schema(example = "2026", nullable = true) Integer sourceYear,
      @Schema(example = "IFRN", nullable = true, description = "Processo seletivo da questão: IFRN ou EAJ (TASK E.1).")
      String institution,
      @Schema(example = "17", nullable = true) Integer sourceQuestionNumber,
      @Schema(nullable = true, description = "Última resposta (NULL = não respondida).")
      String selectedOption,
      @Schema(nullable = true, description = "NULL quando anulada ou não respondida.")
      Boolean isCorrect,
      boolean wasAnnulled,
      @Schema(description = "Gabarito congelado na criação (revelado após encerrar).")
      String frozenAnswerKey,
      @Schema(description = "true quando sem tentativa vinculada a esta execução.")
      boolean unanswered) {}
}
