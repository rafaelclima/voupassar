package br.com.voupassar.questions.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** Alternativa na ordem canônica de rótulo (A–D). */
@Schema(description = "Alternativa de questão objetiva.")
public record QuestionOptionResponse(
    @Schema(example = "A") String label,
    @Schema(example = "informar sobre o aumento do cyberbullying no Brasil.") String text) {}
