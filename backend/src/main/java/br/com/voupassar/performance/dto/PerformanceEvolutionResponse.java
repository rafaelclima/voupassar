package br.com.voupassar.performance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

/**
 * Evolução temporal do aproveitamento (TASK 4.1) — série determinística sobre
 * o fato {@code question_attempts}, sem interpolar lacunas.
 *
 * <p>Baldes em calendário UTC (limitação documentada: o dia local do estudante
 * pode divergir em horas; sem fuso por aluno no MVP). Só baldes com tentativas
 * aparecem, em ordem cronológica. Anuladas seguem a regra geral: contam como
 * conteúdo e ficam fora do aproveitamento.
 */
public record PerformanceEvolutionResponse(
    @Schema(example = "WEEK", allowableValues = {"DAY", "WEEK", "MONTH"}) String granularity,
    List<BucketResponse> buckets,
    List<String> notes) {

  /** Um balde temporal com tentativas (accuracy NULL quando sem pontuáveis). */
  public record BucketResponse(
      @Schema(example = "2026-09-01") LocalDate bucketStart,
      @Schema(example = "6") long attempts,
      @Schema(example = "6") long scored,
      @Schema(example = "4") long correct,
      @Schema(example = "0.6667", nullable = true) Double accuracy) {}
}
