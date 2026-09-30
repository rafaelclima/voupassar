package br.com.voupassar.questions.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Referência curta ao assunto (taxonomia v1.1 observada nas provas). */
@Schema(description = "Assunto da classificação vigente (nulo quando não classificada).")
public record TopicRef(
    @Schema(example = "5") Long id,
    @Schema(example = "ALGEBRA") String code,
    @Schema(example = "Álgebra") String name) {}
