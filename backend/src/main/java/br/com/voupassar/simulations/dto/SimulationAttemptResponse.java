package br.com.voupassar.simulations.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Execução de simulado por disciplina (TASK 5.1) — caderno + estado.
 *
 * <p>Regras de evidência:
 * <ul>
 *   <li>Com {@code status IN_PROGRESS}, o gabarito fica oculto ({@code
 *       frozenAnswerKey}, {@code selectedOption} e {@code isCorrect} nulos):
 *       preserva a sensação de prova nos dois modos. No Modo Estudo o
 *       feedback imediato vem de {@code POST /attempts} (correção do
 *       servidor, TASK 3.7); no Modo Prova a correção aparece só após
 *       concluir (TASK 5.3).</li>
 *   <li>Com {@code status SUBMITTED/ABANDONED}, o caderno revela o gabarito
 *       congelado e a última resposta por questão; o resumo ({@code score})
 *       é calculado no servidor a partir do fato imutável — o cliente nunca
 *       escreve nota.</li>
 *   <li>O enunciado não é duplicado aqui: resolve-se via {@code GET
 *       /api/v1/questions/{id}} (TASK 3.4).</li>
 * </ul>
 */
public record SimulationAttemptResponse(
    @Schema(example = "55") long attemptId,
    @Schema(example = "7") long simulationId,
    @Schema(example = "BY_DISCIPLINE") String type,
    @Schema(example = "Simulado Matemática — 10 questões [PROVA]") String title,
    @Schema(example = "MATEMATICA") String disciplineCode,
    @Schema(example = "Matemática") String disciplineName,
    @Schema(example = "PROVA") String mode,
    @Schema(example = "IN_PROGRESS") String status,
    @Schema(example = "10") int questionCount,
    OffsetDateTime startedAt,
    @Schema(nullable = true) OffsetDateTime submittedAt,
    @Schema(description = "Caderno congelado na ordem das posições (sem enunciado; gabarito só após encerrar).")
    List<CadernoItem> questions,
    @Schema(nullable = true, description = "Resumo calculado no servidor (nulo enquanto IN_PROGRESS).")
    ScoreSummary score,
    List<String> notes) {

  /**
   * Uma posição do caderno congelado. Campos de correção nulos enquanto
   * {@code IN_PROGRESS} (gabarito oculto).
   */
  public record CadernoItem(
      @Schema(example = "1") int position,
      @Schema(example = "12") long questionId,
      @Schema(example = "MATEMATICA") String disciplineCode,
      @Schema(example = "2026", nullable = true) Integer sourceYear,
      @Schema(example = "17", nullable = true) Integer sourceQuestionNumber,
      @Schema(description = "true quando há ao menos uma tentativa vinculada a esta execução.")
      boolean answered,
      @Schema(nullable = true, description = "Gabarito congelado (só após encerrar).")
      String frozenAnswerKey,
      @Schema(nullable = true, description = "Última resposta (só após encerrar).")
      String selectedOption,
      @Schema(nullable = true, description = "NULL quando anulada ou sem resposta.")
      Boolean isCorrect,
      @Schema(description = "Anulada: fora do aproveitamento (pontuação DESCONHECIDA).")
      boolean wasAnnulled) {}

  /** Resumo do desempenho na execução (cálculo do servidor). */
  public record ScoreSummary(
      @Schema(example = "10") long total,
      @Schema(example = "10") long answered,
      @Schema(example = "0") long unanswered,
      @Schema(example = "10") long scored,
      @Schema(example = "7") long correct,
      @Schema(example = "3") long incorrect,
      @Schema(example = "0") long annulled,
      @Schema(example = "0.7", nullable = true,
          description = "correct / scored (NULL quando sem pontuáveis, nunca zero inventado).")
      Double accuracy) {}
}
