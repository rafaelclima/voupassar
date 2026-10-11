package br.com.voupassar.aiprova.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Execução de prova-IA (TASK A.3) — caderno-IA inline + estado.
 *
 * <p>Mesmo contrato de ocultação do simulado oficial: com {@code
 * IN_PROGRESS} o gabarito fica oculto ({@code frozenAnswerKey}, {@code
 * selectedOption} e {@code isCorrect} nulos) nos dois modos. Diferenças
 * honestas para o oficial: o enunciado e as alternativas vêm INLINE (não
 * há {@code GET /questions} para questão-IA — isolamento B.1); cada
 * posição carrega o selo experimental; nunca há ano/número de questão
 * oficial (o formato é <i>inspirado em</i> uma edição, nunca
 * <i>extraído de</i>).
 */
public record AiAttemptResponse(
    @Schema(example = "55") long attemptId,
    @Schema(example = "7") long simulationId,
    @Schema(example = "3") long generationId,
    @Schema(example = "AI_GENERATED_EDITION") String type,
    @Schema(example = "Prova-IA IFRN-2026 (experimental) [PROVA]") String title,
    @Schema(example = "IFRN") String institution,
    @Schema(example = "IFRN-2026", description = "Edição-modelo que inspirou o formato (não é origem).")
    String editionModel,
    @Schema(example = "PROVA") String mode,
    @Schema(example = "IN_PROGRESS") String status,
    @Schema(example = "40") int questionCount,
    OffsetDateTime startedAt,
    @Schema(nullable = true) OffsetDateTime submittedAt,
    @Schema(description = "Caderno-IA congelado na ordem das posições (enunciado inline; gabarito só após encerrar).")
    List<AiCadernoItem> questions,
    @Schema(nullable = true, description = "Resumo calculado no servidor (nulo enquanto IN_PROGRESS).")
    AiScoreSummary score,
    List<String> notes) {

  /**
   * Uma posição do caderno-IA. Selo experimental em cada questão
   * (TASKS.md regra 1 do programa).
   */
  public record AiCadernoItem(
      @Schema(example = "1") int position,
      @Schema(example = "MATEMATICA") String discipline,
      @Schema(example = "ALGEBRA") String topic,
      @Schema(description = "Enunciado auto-contido gerado por IA.")
      String stem,
      @Schema(description = "Alternativas A–D geradas por IA.")
      Map<String, String> options,
      @Schema(description = "true quando há ao menos uma resposta-IA vinculada a esta execução.")
      boolean answered,
      @Schema(nullable = true, description = "Gabarito-IA congelado (só após encerrar; sem garantia pedagógica).")
      String frozenAnswerKey,
      @Schema(nullable = true, description = "Última resposta (só após encerrar).")
      String selectedOption,
      @Schema(nullable = true, description = "NULL enquanto IN_PROGRESS ou sem resposta.")
      Boolean isCorrect,
      @Schema(example = "true", description = "Selo experimental: questão gerada por IA, não oficial.")
      boolean aiGenerated,
      @Schema(example = "Questão gerada por IA — não é questão oficial IFRN/EAJ.")
      String aiNote) {}

  /** Resumo do desempenho na execução-IA (cálculo do servidor). */
  public record AiScoreSummary(
      @Schema(example = "40") long total,
      @Schema(example = "40") long answered,
      @Schema(example = "0") long unanswered,
      @Schema(example = "40") long scored,
      @Schema(example = "30") long correct,
      @Schema(example = "10") long incorrect,
      @Schema(example = "0.75", nullable = true,
          description = "correct / scored (NULL quando sem pontuáveis; placar experimental, sem validade de prova real).")
      Double accuracy) {}
}
