package br.com.voupassar.performance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Desempenho do aluno em um retrato (TASK 4.1) — cálculo determinístico e
 * auditável sobre o fato {@code question_attempts}.
 *
 * <p>Regras de evidência:
 * <ul>
 *   <li>Anuladas contam como conteúdo respondido e ficam fora do
 *       aproveitamento (pontuação DESCONHECIDA, TASK 1.3 §4).</li>
 *   <li>Por disciplina usa {@code questions.discipline_id} (factual); por
 *       assunto/subassunto usa a classificação vigente não-rejeitada mais
 *       recente por questão (derivada, revisão humana PENDENTE — TASK 12.2).
 *       Tentativas sem classificação vigente contam no geral/disciplina e
 *       em {@code unclassifiedAttempts}, nunca em tópico inventado.</li>
 *   <li>{@code accuracy} NULL = sem tentativas pontuáveis no recorte (nunca
 *       zero inventado).</li>
 * </ul>
 */
public record PerformanceOverviewResponse(
    @Schema(example = "42") long totalAttempts,
    @Schema(example = "40") long scoredAttempts,
    @Schema(example = "28") long correct,
    @Schema(example = "12") long incorrect,
    @Schema(example = "2") long annulled,
    @Schema(example = "0.7", nullable = true) Double accuracy,
    @Schema(nullable = true) OffsetDateTime lastAttemptAt,
    @Schema(example = "0",
        description = "Tentativas sem classificação vigente (contam no geral/disciplina, fora de tópico/subtópico).")
    long unclassifiedAttempts,
    List<DisciplinePerformanceResponse> byDiscipline,
    List<TopicPerformanceResponse> byTopic,
    List<SubtopicPerformanceResponse> bySubtopic,
    List<String> notes) {

  /** Aproveitamento em uma disciplina observada nas provas. */
  public record DisciplinePerformanceResponse(
      @Schema(example = "MATEMATICA") String disciplineCode,
      @Schema(example = "Matemática") String disciplineName,
      @Schema(example = "20") long attempts,
      @Schema(example = "19") long scored,
      @Schema(example = "12") long correct,
      @Schema(example = "0.6316", nullable = true) Double accuracy) {}

  /** Aproveitamento em um assunto (classificação vigente, revisão pendente). */
  public record TopicPerformanceResponse(
      @Schema(example = "5") long topicId,
      @Schema(example = "PORCENTAGEM") String topicCode,
      @Schema(example = "Porcentagem") String topicName,
      @Schema(example = "MATEMATICA") String disciplineCode,
      @Schema(example = "Matemática") String disciplineName,
      @Schema(example = "10") long attempts,
      @Schema(example = "10") long scored,
      @Schema(example = "6") long correct,
      @Schema(example = "0.6", nullable = true) Double accuracy) {}

  /** Aproveitamento em um subassunto (só tentativas com subassunto vigente). */
  public record SubtopicPerformanceResponse(
      @Schema(example = "37") long subtopicId,
      @Schema(example = "CALCULO_DIRETO") String subtopicCode,
      @Schema(example = "Cálculo direto") String subtopicName,
      @Schema(example = "5") long topicId,
      @Schema(example = "PORCENTAGEM") String topicCode,
      @Schema(example = "Porcentagem") String topicName,
      @Schema(example = "MATEMATICA") String disciplineCode,
      @Schema(example = "10") long attempts,
      @Schema(example = "10") long scored,
      @Schema(example = "6") long correct,
      @Schema(example = "0.6", nullable = true) Double accuracy) {}
}
