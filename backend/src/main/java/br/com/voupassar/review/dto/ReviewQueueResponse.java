package br.com.voupassar.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Fila de revisão do aluno (TASK 4.5) — leitura determinística, transparente
 * e auditável sobre o fato {@code question_attempts}.
 *
 * <p>Regras de evidência:
 * <ul>
 *   <li>Fonte do desempenho: só {@code question_attempts} (fato imutável) +
 *       a classificação vigente não-rejeitada mais recente por questão para
 *       assunto (derivada, revisão humana PENDENTE — TASK 12.2).</li>
 *   <li>Anuladas ficam fora da fila (pontuação DESCONHECIDA, TASK 1.3 §4):
 *       contam no resumo, nunca como erro nem como acerto.</li>
 *   <li>Só questões já tentadas entram na fila. Questões nunca tentadas
 *       pertencem ao diagnóstico (lacunas, TASK 4.2) e ao roteiro (TASK 4.4),
 *       nunca à revisão.</li>
 *   <li>{@code accuracy} NULL = sem tentativas pontuáveis (nunca zero
 *       inventado). Categoria e motivo derivam das mesmas variáveis que
 *       ordenam a fila — nada inventado.</li>
 *   <li>Dificuldade estimada NÃO é usada (palpite global BAIXA, sem
 *       calibração por desempenho).</li>
 * </ul>
 */
public record ReviewQueueResponse(
    @Schema(example = "12") long totalAttempts,
    @Schema(example = "11") long scoredAttempts,
    @Schema(example = "7") long correct,
    @Schema(example = "4") long incorrect,
    @Schema(example = "1") long annulled,
    @Schema(example = "0.6364", nullable = true) Double accuracy,
    @Schema(description = "Tentativas sem classificação vigente (contam no resumo, entram na fila sem assunto).")
    long unclassifiedAttempts,
    @Schema(description = "Questões pontuáveis distintas já tentadas (tamanho da fila antes do limite).")
    long distinctQuestions,
    @Schema(description = "Itens devolvidos (após filtros e limite).")
    long returned,
    @Schema(example = "20") int limit,
    @Schema(example = "false") boolean onlyErrors,
    @Schema(nullable = true) String disciplineFilter,
    @Schema(nullable = true) Long topicFilter,
    @Schema(description = "Fila ordenada por prioridade determinística (rank 1-based).")
    List<ReviewItem> items,
    List<String> notes) {

  /**
   * Um item da fila de revisão: uma questão já tentada, com o motivo auditável
   * que a colocou naquela posição.
   */
  public record ReviewItem(
      @Schema(example = "1") int rank,
      @Schema(example = "42") long questionId,
      @Schema(example = "MATEMATICA") String disciplineCode,
      @Schema(example = "Matemática") String disciplineName,
      @Schema(example = "2026", nullable = true) Integer sourceYear,
      @Schema(example = "17", nullable = true) Integer sourceQuestionNumber,
      @Schema(example = "5", nullable = true) Long topicId,
      @Schema(example = "PORCENTAGEM", nullable = true) String topicCode,
      @Schema(example = "Porcentagem", nullable = true) String topicName,
      @Schema(nullable = true) Long subtopicId,
      @Schema(nullable = true) String subtopicCode,
      @Schema(nullable = true) String subtopicName,
      @Schema(nullable = true,
          allowableValues = {"PENDING", "REVIEWED", "APPROVED", "REJECTED"},
          description = "Status da classificação vigente (NULL quando sem classificação).")
      String classificationStatus,
      @Schema(nullable = true) String classificationConfidence,
      @Schema(nullable = true) String taxonomyVersion,
      @Schema(example = "3") long attempts,
      @Schema(example = "1") long correct,
      @Schema(example = "2") long incorrect,
      @Schema(description = "Última tentativa pontuável foi acerto? (sempre não-nulo aqui: anuladas ficam fora da fila).")
      boolean lastCorrect,
      @Schema(nullable = true) OffsetDateTime lastAttemptAt,
      @Schema(nullable = true, description = "Dias desde a última tentativa (informativo; não define o balde).")
      Long daysSinceLastAttempt,
      @Schema(example = "FRAGIL",
          allowableValues = {"DOMINADO", "EM_DESENVOLVIMENTO", "FRAGIL", "EM_OBSERVACAO", "NAO_AVALIADO", "NAO_CLASSIFICADO"})
      String topicMastery,
      @Schema(nullable = true) Long topicScored,
      @Schema(nullable = true) Double topicAccuracy,
      @Schema(example = "ERRO_SEM_ACERTO",
          allowableValues = {"ERRO_SEM_ACERTO", "ERRO_RECENTE", "TOPICO_FRAGIL", "REFORCO", "MANUTENCAO", "CONSOLIDADO"})
      String reviewCategory,
      @Schema(description = "Motivo textual gerado dos mesmos fatores (questão + assunto), com números auditáveis.")
      String reason) {}
}
