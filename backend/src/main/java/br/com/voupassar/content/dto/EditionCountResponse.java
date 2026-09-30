package br.com.voupassar.content.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Contagem de um assunto/subassunto dentro de uma edição. */
@Schema(description = "Questões do conteúdo na edição (anuladas contam como conteúdo).")
public record EditionCountResponse(
    @Schema(example = "2026") int year,
    @Schema(example = "4") long questions,
    @Schema(description = "Com X no gabarito — contam como conteúdo, sem pontuar.", example = "0")
        long annulled) {}
