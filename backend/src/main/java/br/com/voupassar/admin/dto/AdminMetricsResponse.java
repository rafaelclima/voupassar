package br.com.voupassar.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Métricas da área administrativa — fotografia do banco de questões para o
 * gestor, sem PII e sem conteúdo de questão.
 *
 * <p>Observabilidade, não fila: desde a decisão de produto 2026-10-06 não há
 * carimbo humano; a confiança vem do veredito do pipeline.
 */
public record AdminMetricsResponse(
    @Schema(example = "240") long questionsTotal,
    @Schema(example = "5") long questionsAnnulled,
    @Schema(example = "36") long questionsWithFigure,
    @Schema(example = "240") long classificationsTotal) {}
