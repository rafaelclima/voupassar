package br.com.voupassar.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

/**
 * Métricas da área administrativa (TASK 12.1) — fotografia do banco de
 * questões para o curador/gestor, sem PII e sem conteúdo de questão.
 */
public record AdminMetricsResponse(
    @Schema(example = "240") long questionsTotal,
    Map<String, Long> questionsByValidation,
    Map<String, Long> questionsByPublication,
    @Schema(example = "5") long questionsAnnulled,
    @Schema(example = "36") long questionsWithFigure,
    @Schema(example = "240") long classificationsTotal,
    Map<String, Long> classificationsByStatus) {}
