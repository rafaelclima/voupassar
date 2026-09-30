package br.com.voupassar.diagnosis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Diagnóstico inicial do aluno (TASK 4.2) — leitura determinística,
 * transparente e auditável sobre o fato {@code question_attempts}.
 *
 * <p>Regras de evidência:
 * <ul>
 *   <li>Fonte do desempenho: só {@code question_attempts} (fato imutável) +
 *       a classificação vigente não-rejeitada mais recente por questão para
 *       assunto (derivada, revisão humana PENDENTE — TASK 12.2).</li>
 *   <li>Fonte histórica: contagens de {@code question_classifications}
 *       não-rejeitadas + {@code questions} por disciplina (derivadas, mesma
 *       revisão pendente). Nada inventado.</li>
 *   <li>Anuladas contam como conteúdo respondido e ficam fora do
 *       aproveitamento (pontuação DESCONHECIDA, TASK 1.3 §4).</li>
 *   <li>{@code accuracy} NULL = sem tentativas pontuáveis no recorte (nunca
 *       zero inventado). Nível de domínio é estimativa inicial com limiares
 *       explícitos, nunca verdade oficial do IFRN.</li>
 *   <li>Dificuldade estimada NÃO é usada (palpite global BAIXA, sem
 *       calibração por dados de desempenho).</li>
 * </ul>
 */
public record DiagnosisResponse(
    @Schema(example = "12") long totalAttempts,
    @Schema(example = "11") long scoredAttempts,
    @Schema(example = "7") long correct,
    @Schema(example = "4") long incorrect,
    @Schema(example = "1") long annulled,
    @Schema(example = "0.6364", nullable = true) Double accuracy,
    @Schema(example = "EM_DESENVOLVIMENTO",
        allowableValues = {"DESCONHECIDO", "INICIAL", "EM_DESENVOLVIMENTO", "CONSOLIDADO"},
        description = "Nível geral estimado a partir do aproveitamento pontuável.")
    String overallLevel,
    @Schema(nullable = true) OffsetDateTime lastAttemptAt,
    @Schema(example = "0",
        description = "Tentativas sem classificação vigente (contam no geral/por disciplina, fora de assunto).")
    long unclassifiedAttempts,
    @Schema(description = "Assuntos com sinal suficiente e aproveitamento >= 70% (ordenados por accuracy desc).")
    List<TopicDiagnosisItem> strengths,
    @Schema(description = "Assuntos com sinal suficiente e aproveitamento < 50% (ordenados por accuracy asc).")
    List<TopicDiagnosisItem> weaknesses,
    @Schema(description = "Assuntos sem nenhuma tentativa pontuável (ordenados por recorrência histórica desc).")
    List<TopicDiagnosisItem> gaps,
    @Schema(description = "Assuntos com 1-2 tentativas pontuáveis: sinal insuficiente (ordenados por recorrência desc).")
    List<TopicDiagnosisItem> lowSignal,
    @Schema(description = "Fila de atenção: todo assunto não-dominado em ordem determinística, com motivo auditável.")
    List<PriorityItem> priorities,
    @Schema(description = "Todos os assuntos da taxonomia com diagnóstico (ordenados por accuracy asc, NULLs por último).")
    List<TopicDiagnosisItem> byTopic,
    List<DisciplineDiagnosisItem> byDiscipline,
    List<String> notes) {

  /** Diagnóstico de um assunto (sempre da taxonomia observada, nunca inventado). */
  public record TopicDiagnosisItem(
      @Schema(example = "5") long topicId,
      @Schema(example = "PORCENTAGEM") String topicCode,
      @Schema(example = "Porcentagem") String topicName,
      @Schema(example = "MATEMATICA") String disciplineCode,
      @Schema(example = "Matemática") String disciplineName,
      @Schema(example = "4") long attempts,
      @Schema(example = "4") long scored,
      @Schema(example = "1") long correct,
      @Schema(example = "0.25", nullable = true) Double accuracy,
      @Schema(example = "12", description = "Questões do assunto no banco (classificações não-rejeitadas).")
      long historicalQuestions,
      @Schema(example = "5.0", description = "Percentual do assunto no banco classificado.")
      double historicalPercent,
      @Schema(example = "6", description = "Edições em que o assunto aparece.")
      int editionsCount,
      @Schema(example = "FRAGIL",
          allowableValues = {"DOMINADO", "EM_DESENVOLVIMENTO", "FRAGIL", "EM_OBSERVACAO", "NAO_AVALIADO"})
      String masteryLevel,
      @Schema(description = "Motivo textual gerado dos mesmos fatores (desempenho + recorrência), com números auditáveis.")
      String reason) {}

  /** Item da fila de atenção (assunto não-dominado, rank 1-based determinístico). */
  public record PriorityItem(
      @Schema(example = "1") int rank,
      @Schema(example = "5") long topicId,
      @Schema(example = "PORCENTAGEM") String topicCode,
      @Schema(example = "Porcentagem") String topicName,
      @Schema(example = "MATEMATICA") String disciplineCode,
      @Schema(example = "Matemática") String disciplineName,
      @Schema(example = "0.25", nullable = true) Double accuracy,
      @Schema(example = "4") long scored,
      @Schema(example = "12") long historicalQuestions,
      @Schema(example = "6") int editionsCount,
      String reason) {}

  /** Diagnóstico por disciplina observada nas provas (factual via questions.discipline_id). */
  public record DisciplineDiagnosisItem(
      @Schema(example = "MATEMATICA") String disciplineCode,
      @Schema(example = "Matemática") String disciplineName,
      @Schema(example = "8") long attempts,
      @Schema(example = "8") long scored,
      @Schema(example = "5") long correct,
      @Schema(example = "0.625", nullable = true) Double accuracy,
      @Schema(example = "120", description = "Questões da disciplina no banco.")
      long historicalQuestions,
      @Schema(example = "EM_DESENVOLVIMENTO",
          allowableValues = {"DOMINADO", "EM_DESENVOLVIMENTO", "FRAGIL", "EM_OBSERVACAO", "NAO_AVALIADO"})
      String masteryLevel,
      String reason) {}
}
