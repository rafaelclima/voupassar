package br.com.voupassar.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Estatísticas + progresso do aluno (TASK 3.6).
 *
 * <p>Derivado só do fato {@code question_attempts} + do agregado
 * {@code student_topic_performance} (Fase 4). Anuladas contam como conteúdo
 * respondido e ficam fora do aproveitamento (regra de pontuação
 * DESCONHECIDA). {@code accuracy} NULL = sem tentativas pontuáveis (nunca
 * zero inventado).
 */
public record ProfileStatsResponse(
    @Schema(example = "42") long totalAttempts,
    @Schema(example = "40") long scoredAttempts,
    @Schema(example = "28") long correct,
    @Schema(example = "12") long incorrect,
    @Schema(example = "2") long annulled,
    @Schema(example = "0.7", nullable = true) Double accuracy,
    @Schema(nullable = true) OffsetDateTime lastAttemptAt,
    List<ModeStatResponse> byMode,
    List<DisciplineStatResponse> byDiscipline,
    List<TopicProgressResponse> topicProgress,
    List<String> notes) {

  /** Aproveitamento em um modo (ESTUDO/PROVA/REVISAO). */
  public record ModeStatResponse(
      @Schema(example = "ESTUDO") String mode,
      @Schema(example = "30") long attempts,
      @Schema(example = "28") long scored,
      @Schema(example = "20") long correct,
      @Schema(example = "0.714", nullable = true) Double accuracy) {}

  /** Aproveitamento em uma disciplina observada nas provas. */
  public record DisciplineStatResponse(
      @Schema(example = "MATEMATICA") String disciplineCode,
      @Schema(example = "Matemática") String disciplineName,
      @Schema(example = "20") long attempts,
      @Schema(example = "19") long scored,
      @Schema(example = "12") long correct,
      @Schema(example = "0.6316", nullable = true) Double accuracy) {}

  /**
   * Linha do agregado por conteúdo (nulo em subassunto = nível tópico).
   * Vazio até o job da TASK 4.1 popular a tabela.
   */
  public record TopicProgressResponse(
      @Schema(example = "PORCENTAGEM") String topicCode,
      @Schema(example = "Porcentagem") String topicName,
      @Schema(example = "LINGUA_PORTUGUESA") String disciplineCode,
      @Schema(example = "CALCULO_DIRETO", nullable = true) String subtopicCode,
      @Schema(example = "Cálculo direto", nullable = true) String subtopicName,
      @Schema(example = "10") int attempts,
      @Schema(example = "6") int hits,
      @Schema(example = "0.6", nullable = true) BigDecimal accuracy,
      @Schema(nullable = true) OffsetDateTime lastAttemptAt) {}
}
