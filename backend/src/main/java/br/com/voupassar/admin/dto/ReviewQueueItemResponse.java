package br.com.voupassar.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Item da fila de revisão (TASK 12.1) — resumo para triagem.
 *
 * <p>O conteúdo integral (enunciado, alternativas, proveniência) sai do
 * {@code GET /api/v1/questions/{id}} existente; aqui vai o necessário para
 * priorizar: origem, disciplina, assunto vigente e os dois status de
 * curadoria. {@code classificationId} é null quando a questão não tem
 * classificação ativa (inconsistência — ver {@code /inconsistencies}).
 */
public record ReviewQueueItemResponse(
    @Schema(example = "1") long id,
    @Schema(example = "OFFICIAL") String sourceType,
    @Schema(example = "2026") Short sourceYear,
    @Schema(example = "21") Short sourceQuestionNumber,
    @Schema(example = "MATEMATICA") String disciplineCode,
    @Schema(example = "Matemática") String disciplineName,
    @Schema(example = "A") String answerKey,
    boolean annulled,
    boolean hasFigure,
    @Schema(example = "PENDING") String validationStatus,
    @Schema(example = "PENDENTE_REVISAO") String publicationStatus,
    @Schema(example = "10") Long classificationId,
    @Schema(example = "ARITMETICA") String topicCode,
    @Schema(example = "SISTEMAS_NUMERACAO") String subtopicCode,
    @Schema(example = "ALTA") String classificationConfidence,
    @Schema(example = "PENDING") String classificationStatus) {}
