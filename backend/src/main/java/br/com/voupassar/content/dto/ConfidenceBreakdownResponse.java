package br.com.voupassar.content.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Distribuição de confiança das classificações.
 *
 * <p>Dificuldade estimada nunca entra aqui (sempre confiança BAIXA, sem dados
 * de desempenho — calibração na Fase 4).
 */
@Schema(description = "Confiança ALTA/MEDIA/BAIXA das classificações do conteúdo.")
public record ConfidenceBreakdownResponse(
    @Schema(example = "14") long alta,
    @Schema(example = "4") long media,
    @Schema(example = "0") long baixa) {}
