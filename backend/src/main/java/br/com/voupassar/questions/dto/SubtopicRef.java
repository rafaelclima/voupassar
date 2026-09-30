package br.com.voupassar.questions.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Referência curta ao subassunto (nulo quando não classificada). */
@Schema(description = "Subassunto da classificação vigente (nulo quando ausente).")
public record SubtopicRef(
    @Schema(example = "25") Long id,
    @Schema(example = "EQUACOES") String code,
    @Schema(example = "Equações") String name) {}
