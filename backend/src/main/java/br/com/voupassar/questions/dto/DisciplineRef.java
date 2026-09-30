package br.com.voupassar.questions.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Referência curta à disciplina (observada nas provas, seed V2). */
@Schema(description = "Disciplina da questão.")
public record DisciplineRef(
    @Schema(example = "MATEMATICA") String code,
    @Schema(example = "Matemática") String name) {}
