package br.com.voupassar.exams.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

/** Versão de uma edição com seus documentos. */
@Schema(description = "Versão (preliminar/definitivo/final) de uma edição.")
public record ExamVersionResponse(
    @Schema(example = "DEFINITIVO") String versionCode,
    @Schema(nullable = true, example = "2025-11-04") LocalDate publishedAt,
    @Schema(nullable = true) String note,
    List<ExamDocumentResponse> documents) {}
