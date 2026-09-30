package br.com.voupassar.exams.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Proposta da produção textual (trechos mínimos de identificação). */
@Schema(description = "Proposta da discursiva. Texto integral fica no PDF-fonte.")
public record EssayPromptResponse(
    @Schema(example = "Artigo de opinião") String genre,
    @Schema(example = "O Brasil deve assumir um papel central no combate às mudanças climáticas?")
        String theme,
    @Schema(example = "Amazonino Belém") String pseudonym,
    String proposalExcerpt,
    @Schema(nullable = true) String criteriaText,
    @Schema(example = "13") int page) {}
