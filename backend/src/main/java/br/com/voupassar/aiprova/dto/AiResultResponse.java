package br.com.voupassar.aiprova.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Resultado de prova-IA (TASK A.3) — correção completa após concluir ou
 * abandonar.
 *
 * <p>Vale a <b>última</b> resposta por posição (fato imutável); sem
 * resposta = não respondida, nunca erro inventado. O placar é contra o
 * gabarito-IA congelado e carrega o selo experimental (sem validade de
 * prova real; não alimenta diagnóstico, roteiro nem revisão).
 */
public record AiResultResponse(
    @Schema(example = "55") long attemptId,
    @Schema(example = "7") long simulationId,
    @Schema(example = "3") long generationId,
    @Schema(example = "Prova-IA IFRN-2026 (experimental) [PROVA]") String title,
    @Schema(example = "IFRN") String institution,
    @Schema(example = "IFRN-2026", description = "Edição-modelo que inspirou o formato (não é origem).")
    String editionModel,
    @Schema(example = "PROVA") String mode,
    @Schema(example = "SUBMITTED") String status,
    @Schema(example = "40") long total,
    @Schema(example = "40") long answered,
    @Schema(example = "0") long unanswered,
    @Schema(example = "40") long scored,
    @Schema(example = "30") long correct,
    @Schema(example = "10") long incorrect,
    @Schema(example = "0.75", nullable = true) Double accuracy,
    @Schema(description = "Correção por posição do caderno-IA (ordem das posições).")
    List<AiResultItem> items,
    List<String> notes) {

  /** Correção de uma posição-IA: última resposta × gabarito congelado. */
  public record AiResultItem(
      @Schema(example = "1") int position,
      @Schema(example = "MATEMATICA") String discipline,
      @Schema(example = "ALGEBRA") String topic,
      @Schema(nullable = true, description = "Última resposta (NULL = não respondida).")
      String selectedOption,
      @Schema(nullable = true, description = "NULL quando não respondida.")
      Boolean isCorrect,
      @Schema(description = "Gabarito-IA congelado na geração (revelado após encerrar; sem garantia pedagógica).")
      String frozenAnswerKey,
      @Schema(description = "true quando sem resposta-IA vinculada a esta execução.")
      boolean unanswered,
      @Schema(example = "true", description = "Selo experimental: questão gerada por IA, não oficial.")
      boolean aiGenerated) {}
}
