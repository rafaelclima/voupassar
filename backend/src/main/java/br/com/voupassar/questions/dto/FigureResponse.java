package br.com.voupassar.questions.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Referência à figura de uma questão oficial (docs/figuras-estrategia.md).
 * Não contém o binário — apenas o caminho relativo ao arquivo no frontend
 * e os metadados de proveniência/revisão.
 */
public record FigureResponse(
    @Schema(description = "Caminho relativo ao arquivo no frontend/assets/figures/.")
        String filePath,
    @Schema(description = "Texto alternativo descritivo (obrigatório).")
        String altText,
    @Schema(description = "Posição quando há múltiplas figuras (1, 2, ...).")
        int position,
    @Schema(description = "Página do caderno-fonte onde a figura aparece.")
        Integer page,
    @Schema(description = "Status de curadoria: PENDENTE_REVISAO ou PUBLICAVEL.")
        String publicationStatus) {}
