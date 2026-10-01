package br.com.voupassar.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Sessão de revisão do aluno (TASK 5.4) — caderno congelado + progresso.
 *
 * <p>Regras de evidência:
 * <ul>
 *   <li>O caderno congela o top-N da fila da TASK 4.5 (ordem determinística)
 *       no momento da criação; o progresso ({@code answered}) deriva das
 *       tentativas com {@code mode=REVISAO} vinculadas a esta sessão
 *       (fato imutável, TASK 3.7).</li>
 *   <li>Feedback imediato: {@code selectedOption}/{@code isCorrect} da última
 *       tentativa <b>nesta sessão</b> são sempre revelados — REVISAO nunca
 *       oculta resultado (a ocultação da TASK 5.3 vale só para PROVA).</li>
 *   <li>{@code reviewCategory}/{@code reason} vêm da fila vigente (mesmas
 *       variáveis que ordenam); quando a questão sai do top-100 vigente, o
 *       item mantém posição e progresso com nota de motivo indisponível
 *       (nunca motivo inventado).</li>
 *   <li>O enunciado não é duplicado aqui: resolve-se via {@code GET
 *       /api/v1/questions/{id}} (TASK 3.4).</li>
 * </ul>
 */
public record ReviewSessionResponse(
    @Schema(example = "12") long sessionId,
    @Schema(example = "REVISAO") String mode,
    @Schema(example = "IN_PROGRESS") String status,
    OffsetDateTime startedAt,
    @Schema(nullable = true) OffsetDateTime finishedAt,
    @Schema(example = "10") int totalItems,
    @Schema(example = "4") long answered,
    @Schema(example = "6") long unanswered,
    @Schema(description = "Caderno congelado na ordem das posições, com progresso e motivo da fila.")
    List<ReviewSessionItem> items,
    List<String> notes) {

  /**
   * Uma posição do caderno de revisão: identidade da questão, motivo da fila
   * e progresso nesta sessão (feedback imediato, sempre revelado).
   */
  public record ReviewSessionItem(
      @Schema(example = "1") int position,
      @Schema(example = "42") long questionId,
      @Schema(example = "MATEMATICA") String disciplineCode,
      @Schema(example = "Matemática") String disciplineName,
      @Schema(example = "2026", nullable = true) Integer sourceYear,
      @Schema(example = "17", nullable = true) Integer sourceQuestionNumber,
      @Schema(nullable = true) String topicCode,
      @Schema(nullable = true) String topicName,
      @Schema(nullable = true,
          allowableValues = {"ERRO_SEM_ACERTO", "ERRO_RECENTE", "TOPICO_FRAGIL",
              "REFORCO", "MANUTENCAO", "CONSOLIDADO"})
      String reviewCategory,
      @Schema(nullable = true,
          description = "Motivo auditável da fila (NULL quando fora do top-100 vigente).")
      String reason,
      @Schema(description = "true quando há ao menos uma tentativa REVISAO vinculada a esta sessão.")
      boolean answered,
      @Schema(nullable = true, description = "Última resposta nesta sessão (NULL = não respondida).")
      String selectedOption,
      @Schema(nullable = true, description = "NULL quando anulada ou não respondida (sempre revelado em REVISAO).")
      Boolean isCorrect,
      @Schema(description = "Anulada: fora do aproveitamento (pontuação DESCONHECIDA).")
      boolean wasAnnulled) {}
}
